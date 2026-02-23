package com.ssafy.questory.auth.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LoginPrincipal implements OAuth2User {
    private final Long memberId;
    private final String email;

    public LoginPrincipal(Long memberId, String email) {
        this.memberId = memberId;
        this.email = email;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "memberId", memberId,
                "email", email
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return String.valueOf(memberId);
    }
}
