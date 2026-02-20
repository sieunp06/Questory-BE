package com.ssafy.questory.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.config.security.jwt.JwtAuthenticationEntryPoint;
import com.ssafy.questory.config.security.jwt.JwtAuthenticationFilter;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MemberController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationEntryPoint.class)
        }
)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    MemberService memberService;

    @Test
    @DisplayName("회원가입 성공 - 201 + REST Docs")
    void member_register_success() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!aaaa",
                "테스터"
        );

        MemberResponseDto response = MemberResponseDto.builder()
                .memberId(1L)
                .email("test@example.com")
                .nickname("테스터")
                .build();

        given(memberService.register(any(RegisterRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.member_id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"))
                .andDo(document("member-register",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email")
                                        .description("회원 이메일")
                                        .attributes(key("constraints").value("NotBlank, Email")),
                                fieldWithPath("password")
                                        .description("비밀번호")
                                        .attributes(key("constraints").value("8~64, 대/소문자/숫자/특수문자 포함")),
                                fieldWithPath("password_confirm")
                                        .description("비밀번호 확인 (snake_case)")
                                        .attributes(key("constraints").value("NotBlank, 8~64")),
                                fieldWithPath("nickname")
                                        .description("닉네임")
                                        .attributes(key("constraints").value("2~20, 한글/영문/숫자/._- 허용"))
                        ),
                        responseFields(
                                fieldWithPath("member_id").description("회원 ID"),
                                fieldWithPath("email").description("회원 이메일"),
                                fieldWithPath("nickname").description("회원 닉네임")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 실패 - Validation 400 + REST Docs")
    void member_register_validation_fail() throws Exception {
        String invalidJson = """
                {
                  "email": "invalid-email",
                  "password": "123",
                  "password_confirm": "123",
                  "nickname": "a"
                }
                """;

        mockMvc.perform(post("/api/member/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(document("member-register-400",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }
}
