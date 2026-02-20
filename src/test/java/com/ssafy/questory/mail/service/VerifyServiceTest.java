package com.ssafy.questory.mail.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.mail.common.RedisUtil;
import com.ssafy.questory.mail.dto.request.EmailVerificationRequestDto;
import com.ssafy.questory.mail.dto.response.MailResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyServiceTest {

    @Mock
    RedisUtil redisUtil;

    @InjectMocks
    VerifyService verifyService;

    @Test
    void buildMail_whenCooldownExists_throwVerificationCooldown() {
        String email = "user@example.com";
        when(redisUtil.exists("VERIFICATION:COOLDOWN:" + email)).thenReturn(true);

        Throwable t = catchThrowable(() -> verifyService.buildMail(email));

        assertThat(t).isInstanceOf(CustomException.class);
        verify(redisUtil, never()).setDataExpire(startsWith("VERIFICATION:CODE:"), any(), anyLong());
    }

    @Test
    void buildMail_whenNoCooldown_savesCodeTryCooldownAndReturnsMail() {
        String email = "user@example.com";
        when(redisUtil.exists("VERIFICATION:COOLDOWN:" + email)).thenReturn(false);

        MailResponseDto mail = verifyService.buildMail(email);

        assertThat(mail.email()).isEqualTo(email);
        assertThat(mail.title()).contains("Questory");
        assertThat(mail.content()).contains("인증 코드:");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);

        verify(redisUtil).setDataExpire(eq("VERIFICATION:CODE:" + email), codeCaptor.capture(), eq(300L));
        String verificationCode = codeCaptor.getValue();

        assertThat(verificationCode).matches("^[A-Z0-9]{6}$");
        assertThat(mail.content()).contains(verificationCode);

        verify(redisUtil).setDataExpire("VERIFICATION:TRY:" + email, "0", 300L);
        verify(redisUtil).setDataExpire("VERIFICATION:COOLDOWN:" + email, "1", 60L);
    }

    @Test
    void checkVerifyCode_whenCodeExpired_throwExpired() {
        EmailVerificationRequestDto dto = new EmailVerificationRequestDto("user@example.com", "ABC123");
        when(redisUtil.getData("VERIFICATION:CODE:" + dto.email())).thenReturn(null);

        Throwable t = catchThrowable(() -> verifyService.checkVerifyCode(dto));

        assertThat(t).isInstanceOf(CustomException.class);
        verify(redisUtil, never()).deleteData(anyString());
    }

    @Test
    void checkVerifyCode_whenMismatch_throwMismatch() {
        EmailVerificationRequestDto dto = new EmailVerificationRequestDto("user@example.com", "ABC123");
        when(redisUtil.getData("VERIFICATION:CODE:" + dto.email())).thenReturn("ZZZ999");

        Throwable t = catchThrowable(() -> verifyService.checkVerifyCode(dto));

        assertThat(t).isInstanceOf(CustomException.class);
        verify(redisUtil, never()).deleteData(anyString());
        verify(redisUtil, never()).setDataExpire(startsWith("VERIFICATION:SUCCESS:"), anyString(), anyLong());
    }

    @Test
    void checkVerifyCode_whenMatch_deleteCodeAndSetSuccess() {
        EmailVerificationRequestDto dto = new EmailVerificationRequestDto("user@example.com", "ABC123");
        when(redisUtil.getData("VERIFICATION:CODE:" + dto.email())).thenReturn("ABC123");

        verifyService.checkVerifyCode(dto);

        verify(redisUtil).deleteData("VERIFICATION:CODE:" + dto.email());
        verify(redisUtil).setDataExpire("VERIFICATION:SUCCESS:" + dto.email(), "true", 1800L);
    }
}