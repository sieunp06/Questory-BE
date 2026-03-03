package com.ssafy.questory.group.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAndUpdateDto(
        @NotBlank(message = "파티 이름은 필수입니다.")
        @Size(max = 100, message = "파티 이름은 100자 이하여야 합니다.")
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9\\s]+$",
                message = "파티 이름에는 특수문자를 사용할 수 없습니다."
        )
        String name
) {}
