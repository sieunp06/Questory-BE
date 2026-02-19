package com.ssafy.questory.mail.dto.response;

import lombok.Builder;

@Builder
public record MailResponseDto(
    String email,
    String title,
    String content
) {}
