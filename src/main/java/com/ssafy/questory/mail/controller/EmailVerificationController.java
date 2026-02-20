package com.ssafy.questory.mail.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.mail.dto.request.EmailVerificationRequestDto;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.mail.service.MailSendService;
import com.ssafy.questory.mail.service.VerifyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/email")
@RestController
public class EmailVerificationController {
    private final MailSendService mailSendService;
    private final VerifyService verifyService;

    @PostMapping("/send-verify")
    public ResponseEntity<ApiResponse<Void>> sendVerifyEmail(@Valid @RequestBody MemberEmailRequestDto dto) {
        mailSendService.sendEmail(verifyService.buildMail(dto.email()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok("인증 코드를 이메일로 전송하였습니다."));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@Valid @RequestBody EmailVerificationRequestDto dto) {
        verifyService.checkVerifyCode(dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("인증이 완료되었습니다."));
    }
}
