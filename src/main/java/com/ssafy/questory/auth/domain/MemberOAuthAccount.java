package com.ssafy.questory.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberOAuthAccount {
    private Long memberOAuthAccountId;
    private Long memberId;
    private AuthProvider provider;
    private String providerMemberId;
    private Boolean emailVerified;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
}
