package com.ssafy.questory.auth.util;

import com.ssafy.questory.auth.oauth2.KakaoUserInfo;
import com.ssafy.questory.auth.oauth2.NaverUserInfo;
import com.ssafy.questory.auth.oauth2.OAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    private OAuth2UserInfoFactory() {}

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        String key = registrationId == null ? "" : registrationId.trim().toLowerCase();

        return switch (key) {
            case "kakao"  -> new KakaoUserInfo(attributes);
            case "naver"  -> new NaverUserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}
