package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.config.jwt.JwtService;
import com.ssafy.questory.auth.config.jwt.JwtUserDetailsService;
import com.ssafy.questory.auth.dto.TicketExchangeRequestDto;
import com.ssafy.questory.auth.ticket.PkceUtil;
import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final TicketStore ticketStore;
    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final PkceUtil pkceUtil;

    public String exchangeTicket(TicketExchangeRequestDto dto) {
        String ticket = dto.ticket();
        String codeVerifier = dto.codeVerifier();

        TicketPayload payload = ticketStore.consume(ticket);

        if (payload == null) {
            throw new CustomException(ErrorCode.INVALID_TICKET);
        }

        String storedCodeChallenge = payload.codeChallenge();
        if (StringUtils.hasText(storedCodeChallenge)) {
            if (!StringUtils.hasText(codeVerifier)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            String calculatedCodeChallenge = pkceUtil.generateCodeChallenge(codeVerifier);

            if (!storedCodeChallenge.equals(calculatedCodeChallenge)) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByEmail(payload.email());
        return jwtService.generateAccessToken(userDetails);
    }
}
