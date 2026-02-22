package com.ssafy.questory.auth.oauth2;

public interface OAuth2UserInfo {
    String getProviderUserId();
    String getEmail();
    String getNickname();
}
