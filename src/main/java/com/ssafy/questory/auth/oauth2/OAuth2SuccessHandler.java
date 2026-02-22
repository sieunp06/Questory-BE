package com.ssafy.questory.auth.oauth2;

import com.ssafy.questory.auth.domain.LoginPrincipal;
import com.ssafy.questory.auth.ticket.TicketPayload;
import com.ssafy.questory.auth.ticket.TicketStore;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.security.config.jwt.JwtService;
import com.ssafy.questory.security.config.jwt.JwtUserDetailsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final TicketStore ticketStore;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();

        Long memberId;
        String email;

        if (principal instanceof LoginPrincipal loginPrincipal) {
            memberId = loginPrincipal.getMemberId();
            email = loginPrincipal.getEmail();
        } else if (principal instanceof OidcUser oidcUser) {
            Object mid = oidcUser.getClaims().get("memberId");
            if (mid == null) throw new CustomException(ErrorCode.UNAUTHORIZED);
            memberId = Long.valueOf(String.valueOf(mid));
            email = oidcUser.getEmail();
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        UserDetails userDetails = jwtUserDetailsService.loadUserByEmail(email);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMillis(jwtService.getRefreshExpMs()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String ticket = ticketStore.issue(new TicketPayload(memberId, email));

        String redirectUrl = UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("ticket", ticket)
                .build()
                .toUriString();

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }
}
