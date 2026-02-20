package com.ssafy.questory.mail.service;

import com.ssafy.questory.mail.dto.response.MailResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailSendServiceTest {

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    MailSendService mailSendService;

    @Test
    void sendEmail_sendsSimpleMailMessage_withToFromText() {
        ReflectionTestUtils.setField(mailSendService, "fromEmail", "noreply@questory.com");

        MailResponseDto dto = MailResponseDto.builder()
                .email("user@example.com")
                .title("[Questory] 이메일 인증 코드 안내")
                .content("인증 코드: ABC123")
                .build();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        mailSendService.sendEmail(dto);

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage msg = captor.getValue();

        assertThat(msg.getTo()).containsExactly("user@example.com");
        assertThat(msg.getFrom()).isEqualTo("noreply@questory.com");
        assertThat(msg.getText()).isEqualTo("인증 코드: ABC123");

        assertThat(msg.getSubject()).isNull();
    }
}