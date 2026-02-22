package com.ssafy.questory.mail.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EmailVerificationRequestDto(
        @NotBlank @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[A-Z0-9]{6}$",
                message = "인증 코드는 6자리이며 대문자(A-Z)와 숫자(0-9)만 사용할 수 있습니다."
        )
        String code
) {}
