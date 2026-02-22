package com.ssafy.questory.auth.oauth2;

import java.util.Map;

import static com.ssafy.questory.auth.util.OAuth2AttributeUtils.asMap;
import static com.ssafy.questory.auth.util.OAuth2AttributeUtils.asString;

public class NaverUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final Map<String, Object> response;

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = asMap(attributes.get("response"));
    }

    @Override
    public String getProviderUserId() {
        if (response == null) return null;
        return asString(response.get("id"));
    }

    @Override
    public String getEmail() {
        if (response == null) return null;
        return asString(response.get("email"));
    }

    @Override
    public String getNickname() {
        if (response == null) return null;

        String nickname = asString(response.get("nickname"));
        if (nickname != null && !nickname.isBlank()) return nickname;

        String name = asString(response.get("name"));
        if (name != null && !name.isBlank()) return name;

        String email = getEmail();
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf('@'));

        return null;
    }
}