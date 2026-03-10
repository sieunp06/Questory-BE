package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.config.jwt.JwtService;
import com.ssafy.questory.auth.config.jwt.JwtUserDetailsService;
import com.ssafy.questory.auth.dto.TicketExchangeRequestDto;
import com.ssafy.questory.auth.ticket.PkceUtil;
import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private TicketStore ticketStore;

    @Mock
    private JwtUserDetailsService jwtUserDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PkceUtil pkceUtil;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("exchangeTicket")
    class ExchangeTicketTest {

        @Test
        @DisplayName("PKCE가 없는 ticket이면 그대로 Access Token을 발급한다")
        void exchangeTicket_success_without_pkce() {
            TicketExchangeRequestDto dto = new TicketExchangeRequestDto("ticket-123", null);
            TicketPayload payload = new TicketPayload(1L, "user@test.com", null);
            UserDetails userDetails = User.withUsername("user@test.com")
                    .password("noop")
                    .authorities("ROLE_USER")
                    .build();

            given(ticketStore.consume("ticket-123")).willReturn(payload);
            given(jwtUserDetailsService.loadUserByEmail("user@test.com")).willReturn(userDetails);
            given(jwtService.generateAccessToken(userDetails)).willReturn("access-token");

            String result = authService.exchangeTicket(dto);

            assertThat(result).isEqualTo("access-token");
            then(pkceUtil).should(never()).generateCodeChallenge(anyString());
        }

        @Test
        @DisplayName("PKCE 검증에 성공하면 Access Token을 발급한다")
        void exchangeTicket_success_with_pkce() {
            TicketExchangeRequestDto dto = new TicketExchangeRequestDto("ticket-123", "plain-verifier");
            TicketPayload payload = new TicketPayload(1L, "user@test.com", "hashed-challenge");
            UserDetails userDetails = User.withUsername("user@test.com")
                    .password("noop")
                    .authorities("ROLE_USER")
                    .build();

            given(ticketStore.consume("ticket-123")).willReturn(payload);
            given(pkceUtil.generateCodeChallenge("plain-verifier")).willReturn("hashed-challenge");
            given(jwtUserDetailsService.loadUserByEmail("user@test.com")).willReturn(userDetails);
            given(jwtService.generateAccessToken(userDetails)).willReturn("access-token");

            String result = authService.exchangeTicket(dto);

            assertThat(result).isEqualTo("access-token");
            then(pkceUtil).should().generateCodeChallenge("plain-verifier");
        }

        @Test
        @DisplayName("유효하지 않은 ticket이면 INVALID_TICKET 예외를 던진다")
        void exchangeTicket_fail_when_ticket_invalid() {
            TicketExchangeRequestDto dto = new TicketExchangeRequestDto("invalid-ticket", "plain-verifier");
            given(ticketStore.consume("invalid-ticket")).willReturn(null);

            assertThatThrownBy(() -> authService.exchangeTicket(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_TICKET);

            then(jwtUserDetailsService).shouldHaveNoInteractions();
            then(jwtService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("PKCE가 필요한 ticket인데 codeVerifier가 없으면 UNAUTHORIZED 예외를 던진다")
        void exchangeTicket_fail_when_codeVerifier_missing() {
            TicketExchangeRequestDto dto = new TicketExchangeRequestDto("ticket-123", null);
            TicketPayload payload = new TicketPayload(1L, "user@test.com", "hashed-challenge");

            given(ticketStore.consume("ticket-123")).willReturn(payload);

            assertThatThrownBy(() -> authService.exchangeTicket(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED);

            then(pkceUtil).shouldHaveNoInteractions();
            then(jwtUserDetailsService).shouldHaveNoInteractions();
            then(jwtService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("PKCE 검증값이 일치하지 않으면 UNAUTHORIZED 예외를 던진다")
        void exchangeTicket_fail_when_pkce_mismatch() {
            TicketExchangeRequestDto dto = new TicketExchangeRequestDto("ticket-123", "plain-verifier");
            TicketPayload payload = new TicketPayload(1L, "user@test.com", "stored-challenge");

            given(ticketStore.consume("ticket-123")).willReturn(payload);
            given(pkceUtil.generateCodeChallenge("plain-verifier")).willReturn("different-challenge");

            assertThatThrownBy(() -> authService.exchangeTicket(dto))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.UNAUTHORIZED);

            then(jwtUserDetailsService).shouldHaveNoInteractions();
            then(jwtService).shouldHaveNoInteractions();
        }
    }
}