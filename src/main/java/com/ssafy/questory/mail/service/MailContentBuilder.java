package com.ssafy.questory.mail.service;

import com.ssafy.questory.mail.dto.response.MailResponseDto;

public interface MailContentBuilder {
    MailResponseDto buildMail(String email);
}
