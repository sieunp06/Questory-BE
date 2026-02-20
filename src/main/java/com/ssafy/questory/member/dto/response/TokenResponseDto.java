package com.ssafy.questory.member.dto.response;

import lombok.Builder;

@Builder
public record TokenResponseDto(
        String email,
        String accessToken,
        String refreshToken
) {}
