package com.ssafy.questory.auth.dto;

public record AccessTokenResponseDto(
        String accessToken,
        long redirectUrl
) {}
