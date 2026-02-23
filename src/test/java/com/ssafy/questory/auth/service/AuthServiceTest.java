package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.security.config.jwt.JwtService;
import com.ssafy.questory.security.config.jwt.JwtUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock TicketStore ticketStore;
    @Mock JwtService jwtService;
    @Mock JwtUserDetailsService jwtUserDetailsService;

    @InjectMocks AuthService authService;

    @Test
    void exchangeTicket_success_returnsAccessToken() {
        // given
        String ticket = "TICKET-123";
        String email = "user@example.com";

        // TicketPayload이 record/class 무엇이든 상관없이 "email()"만 쓸 수 있게 스텁
        TicketPayload payload = mock(TicketPayload.class);
        when(payload.email()).thenReturn(email);

        UserDetails userDetails = mock(UserDetails.class);

        when(ticketStore.consume(ticket)).thenReturn(payload);
        when(jwtUserDetailsService.loadUserByEmail(email)).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access.token.value");

        // when
        String accessToken = authService.exchangeTicket(ticket);

        // then
        assertThat(accessToken).isEqualTo("access.token.value");

        verify(ticketStore).consume(ticket);
        verify(jwtUserDetailsService).loadUserByEmail(email);
        verify(jwtService).generateAccessToken(userDetails);
        verifyNoMoreInteractions(ticketStore, jwtUserDetailsService, jwtService);
    }

    @Test
    void exchangeTicket_invalidTicket_throwsCustomException_andDoesNotCallJwt() {
        // given
        String ticket = "INVALID";
        when(ticketStore.consume(ticket)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.exchangeTicket(ticket))
                .isInstanceOf(CustomException.class);

        // 핵심: payload가 null이면 이후 로직이 절대 돌면 안 됨
        verify(ticketStore).consume(ticket);
        verifyNoInteractions(jwtUserDetailsService, jwtService);
    }
}