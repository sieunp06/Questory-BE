package com.ssafy.questory.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPasswordCredentials {
    private Long memberId;
    private String passwordHash;
    private LocalDateTime passwordUpdatedAt;
    private Integer failedLoginCount;
    private LocalDateTime lastFailedLoginAt;
    private LocalDateTime lockedUntil;
}
