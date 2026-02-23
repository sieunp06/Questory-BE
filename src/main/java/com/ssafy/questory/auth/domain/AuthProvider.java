package com.ssafy.questory.auth.domain;

public enum AuthProvider {
    KAKAO,
    GOOGLE,
    NAVER;

    public static AuthProvider from(String raw) {
        return AuthProvider.valueOf(raw.trim().toUpperCase());
    }
}
