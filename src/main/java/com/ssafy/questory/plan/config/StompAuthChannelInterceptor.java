package com.ssafy.questory.plan.config;

import com.ssafy.questory.auth.config.jwt.JwtService;
import com.ssafy.questory.member.domain.SecurityMember;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearer = accessor.getFirstNativeHeader("Authorization");
            if (bearer == null || !bearer.startsWith("Bearer ")) {
                throw new JwtException("JWT 토큰이 없습니다.");
            }

            String token = bearer.substring(7);
            String username = jwtService.extractUsername(token, JwtService.TokenType.ACCESS);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!(userDetails instanceof SecurityMember securityMember)) {
                throw new JwtException("인증 사용자 타입이 올바르지 않습니다.");
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            securityMember,
                            null,
                            securityMember.getAuthorities()
                    );

            accessor.setUser(authentication);
        }

        return message;
    }
}