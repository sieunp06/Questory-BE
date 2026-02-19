package com.ssafy.questory.mail.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.mail.common.RedisUtil;
import com.ssafy.questory.mail.dto.request.EmailVerificationRequestDto;
import com.ssafy.questory.mail.dto.response.MailResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class VerifyService implements MailContentBuilder {

    private final RedisUtil redisUtil;

    @Override
    public MailResponseDto buildMail(String email) {
        if (redisUtil.exists("VERIFICATION:COOLDOWN:" + email)) {
            throw new CustomException(ErrorCode.VERIFICATION_COOLDOWN);
        }

        String verificationCode = generateVerificationCode();

        redisUtil.setDataExpire("VERIFICATION:CODE:" + email, verificationCode, 300L);
        redisUtil.setDataExpire("VERIFICATION:TRY:" + email, "0", 300L);
        redisUtil.setDataExpire("VERIFICATION:COOLDOWN:" + email, "1", 60L);

        String title = "[Questory] 이메일 인증 코드 안내";
        String content = String.format("""
            안녕하세요, Questory입니다.

            요청하신 이메일 인증 코드를 안내드립니다.
            회원가입을 완료하려면 아래 인증 코드를 입력해주세요.

            인증 코드: %s

            해당 코드는 5분간 유효하며, 만료 후에는 다시 요청하셔야 합니다.

            감사합니다.
            """, verificationCode);

        return MailResponseDto.builder()
                .email(email)
                .title(title)
                .content(content)
                .build();
    }

    private String generateVerificationCode() {
        int length = 6;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(charSet.length());
            code.append(charSet.charAt(idx));
        }

        return code.toString();
    }

    public void checkVerifyCode(EmailVerificationRequestDto dto) {
        String email = dto.email();
        String inputCode = dto.code();

        String codeKey = "VERIFICATION:CODE:" + email;

        String savedCode = redisUtil.getData(codeKey);

        if (savedCode == null) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }

        if (!savedCode.equals(inputCode)) {
            throw new CustomException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }

        redisUtil.deleteData(codeKey);
        redisUtil.setDataExpire("VERIFICATION:SUCCESS:" + email, "true", 1800L);
    }
}