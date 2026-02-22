package com.ssafy.questory.auth.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {
    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String provider = extractProvider(request);
        String errorCode = "OAUTH2_LOGIN_FAILED";

        if (exception instanceof OAuth2AuthenticationException oae) {
            if (oae.getError() != null && oae.getError().getErrorCode() != null) {
                errorCode = oae.getError().getErrorCode();
            }
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(redirectUri)
                .queryParam("provider", provider)
                .queryParam("error", errorCode)
                .build(true)
                .toUriString();

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader(HttpHeaders.LOCATION, redirectUrl);
    }

    private String extractProvider(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String marker = "/login/oauth2/code/";
        int idx = uri.indexOf(marker);
        if (idx >= 0) {
            return uri.substring(idx + marker.length());
        }
        return "unknown";
    }
}
