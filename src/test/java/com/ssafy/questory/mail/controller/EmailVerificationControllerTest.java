package com.ssafy.questory.mail.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.mail.dto.request.EmailVerificationRequestDto;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.mail.dto.response.MailResponseDto;
import com.ssafy.questory.mail.service.MailSendService;
import com.ssafy.questory.mail.service.VerifyService;
import com.ssafy.questory.security.config.SecurityConfig;
import com.ssafy.questory.security.config.jwt.JwtAuthenticationEntryPoint;
import com.ssafy.questory.security.config.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = EmailVerificationController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationEntryPoint.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class) // 네 프로젝트 SecurityConfig 클래스
        }
)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class EmailVerificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean MailSendService mailSendService;
    @MockitoBean VerifyService verifyService;

    @Test
    @DisplayName("이메일 인증코드 전송 성공 - 202 ACCEPTED")
    void send_verify_email_success() throws Exception {
        MemberEmailRequestDto request =
                new MemberEmailRequestDto("test@example.com");

        MailResponseDto mail = MailResponseDto.builder()
                .email("test@example.com")
                .title("Questory 이메일 인증")
                .content("인증코드: ABC123")
                .build();

        given(verifyService.buildMail("test@example.com"))
                .willReturn(mail);

        willDoNothing().given(mailSendService).sendEmail(mail);

        mockMvc.perform(post("/api/email/send-verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andDo(document("email-send-verify",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email")
                                        .description("인증 코드를 받을 이메일")
                                        .attributes(key("constraints").value("NotBlank, Email"))
                        )
                ));

        then(verifyService).should(times(1)).buildMail("test@example.com");
        then(mailSendService).should(times(1)).sendEmail(mail);
    }

    @Test
    @DisplayName("이메일 인증코드 검증 성공 - 200 OK")
    void verify_code_success() throws Exception {
        EmailVerificationRequestDto request =
                new EmailVerificationRequestDto("test@example.com", "ABC123");

        willDoNothing().given(verifyService)
                .checkVerifyCode(any(EmailVerificationRequestDto.class));

        mockMvc.perform(post("/api/email/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("email-verify-code",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email")
                                        .description("인증 대상 이메일")
                                        .attributes(key("constraints").value("NotBlank, Email")),
                                fieldWithPath("code")
                                        .description("이메일로 받은 6자리 인증 코드")
                                        .attributes(key("constraints").value("NotBlank, ^[A-Z0-9]{6}$"))
                        )
                ));

        then(verifyService).should(times(1))
                .checkVerifyCode(any(EmailVerificationRequestDto.class));
        then(mailSendService).shouldHaveNoInteractions();
    }
}