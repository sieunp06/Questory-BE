package com.ssafy.questory.member.dto.security;

import java.time.LocalDateTime;

public record LoginPrincipalRow(
        Long memberId,
        String email,
        String nickname,
        String status,
        String passwordHash,
        Integer failedLoginCount,
        LocalDateTime lockedUntil
) {
}