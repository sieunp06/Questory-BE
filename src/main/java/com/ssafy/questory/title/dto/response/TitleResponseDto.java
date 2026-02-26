package com.ssafy.questory.title.dto.response;

import java.time.LocalDateTime;

public record TitleResponseDto(
        Long titleId,
        String name,
        LocalDateTime acquiredAt,
        boolean representative
) {}
