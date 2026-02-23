package com.ssafy.questory.auth.oauth2;

import java.util.Map;

import static com.ssafy.questory.auth.util.OAuth2AttributeUtils.asMap;
import static com.ssafy.questory.auth.util.OAuth2AttributeUtils.asString;

public class KakaoUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderUserId() {
        return asString(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> account = asMap(attributes.get("kakao_account"));
        if (account == null) return null;
        return asString(account.get("email"));
    }

    @Override
    public String getNickname() {
        Map<String, Object> account = asMap(attributes.get("kakao_account"));
        if (account == null) return null;

        Map<String, Object> profile = asMap(account.get("profile"));
        if (profile == null) return null;

        return asString(profile.get("nickname"));
    }
}
