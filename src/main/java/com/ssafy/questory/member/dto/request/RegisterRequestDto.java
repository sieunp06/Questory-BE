package com.ssafy.questory.member.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 64)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$",
                message = "비밀번호는 8~64자이며 대문자/소문자/숫자/특수문자를 모두 포함해야 합니다."
        )
        String password,

        @NotBlank
        @Size(min = 8, max = 64)
        @JsonProperty("password_confirm")
        String passwordConfirm,

        @NotBlank
        @Size(min = 2, max = 20)
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9._-]{2,20}$",
                message = "닉네임은 2~20자이며 한글/영문/숫자/일부 특수문자(._-)만 허용됩니다."
        )
        String nickname
) {}
