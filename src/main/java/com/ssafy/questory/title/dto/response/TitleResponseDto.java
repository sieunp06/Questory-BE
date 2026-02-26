package com.ssafy.questory.title.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TitleResponseDto(
        Long titleId,
        String name,
        LocalDateTime acquiredAt,
        boolean representative
) {}
