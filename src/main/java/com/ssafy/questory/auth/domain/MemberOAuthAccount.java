package com.ssafy.questory.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberOAuthAccount {
    private Long memberOAuthAccountId;
    private Long memberId;
    private AuthProvider provider;
    private String providerMemberId;
}
