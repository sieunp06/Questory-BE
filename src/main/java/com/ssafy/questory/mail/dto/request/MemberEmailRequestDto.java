package com.ssafy.questory.mail.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MemberEmailRequestDto(
        @NotBlank @Email
        String email
) {}
