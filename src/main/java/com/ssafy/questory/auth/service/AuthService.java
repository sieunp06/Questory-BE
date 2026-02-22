package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.security.config.jwt.JwtService;
import com.ssafy.questory.security.config.jwt.JwtUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TicketStore ticketStore;
    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;

    public String exchangeTicket(String ticket) {
        TicketPayload payload = ticketStore.consume(ticket);

        if (payload == null) {
            throw new CustomException(ErrorCode.INVALID_TICKET);
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByEmail(payload.email());
        return jwtService.generateAccessToken(userDetails);
    }
}
