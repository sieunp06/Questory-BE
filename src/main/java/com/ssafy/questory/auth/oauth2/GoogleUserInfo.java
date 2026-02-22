package com.ssafy.questory.auth.oauth2;

import java.util.Map;

import static com.ssafy.questory.auth.util.OAuth2AttributeUtils.asString;

public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderUserId() {
        return asString(attributes.get("sub"));
    }

    @Override
    public String getEmail() {
        return asString(attributes.get("email"));
    }

    @Override
    public String getNickname() {
        String name = asString(attributes.get("name"));
        if (name != null && !name.isBlank()) return name;

        String givenName = asString(attributes.get("given_name"));
        if (givenName != null && !givenName.isBlank()) return givenName;

        String email = getEmail();
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf('@'));

        return null;
    }
}