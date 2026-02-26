package com.ssafy.questory.title.dto.request;

import jakarta.validation.constraints.NotNull;

public record AcquireTitleRequestDto(
        @NotNull Long titleId
) {}
