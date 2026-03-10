package com.ssafy.questory.auth.config;

import com.ssafy.questory.auth.ticket.PkceChallengeStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

@Configuration
@RequiredArgsConstructor
public class OAuth2PkceConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final PkceChallengeStore pkceChallengeStore;

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver() {
        return new CustomAuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization",
                pkceChallengeStore
        );
    }
}