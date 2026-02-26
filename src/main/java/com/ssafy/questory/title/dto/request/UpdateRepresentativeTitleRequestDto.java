package com.ssafy.questory.title.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRepresentativeTitleRequestDto(
        @NotNull Long titleId
) {}
