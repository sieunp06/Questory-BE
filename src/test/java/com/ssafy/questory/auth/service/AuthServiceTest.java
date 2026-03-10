package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.auth.config.jwt.JwtService;
import com.ssafy.questory.auth.config.jwt.JwtUserDetailsService;
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
        String ticket = "TICKET-123";
        String email = "user@example.com";

        TicketPayload payload = mock(TicketPayload.class);
        when(payload.email()).thenReturn(email);

        UserDetails userDetails = mock(UserDetails.class);

        when(ticketStore.consume(ticket)).thenReturn(payload);
        when(jwtUserDetailsService.loadUserByEmail(email)).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access.token.value");

        String accessToken = authService.exchangeTicket(ticket);

        assertThat(accessToken).isEqualTo("access.token.value");

        verify(ticketStore).consume(ticket);
        verify(jwtUserDetailsService).loadUserByEmail(email);
        verify(jwtService).generateAccessToken(userDetails);
        verifyNoMoreInteractions(ticketStore, jwtUserDetailsService, jwtService);
    }

    @Test
    void exchangeTicket_invalidTicket_throwsCustomException_andDoesNotCallJwt() {
        String ticket = "INVALID";
        when(ticketStore.consume(ticket)).thenReturn(null);

        assertThatThrownBy(() -> authService.exchangeTicket(ticket))
                .isInstanceOf(CustomException.class);

        verify(ticketStore).consume(ticket);
        verifyNoInteractions(jwtUserDetailsService, jwtService);
    }
}