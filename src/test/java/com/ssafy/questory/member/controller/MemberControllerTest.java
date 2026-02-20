package com.ssafy.questory.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.member.dto.request.LoginRequestDto;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.dto.response.TokenResponseDto;
import com.ssafy.questory.member.service.MemberService;
import com.ssafy.questory.security.config.jwt.JwtAuthenticationEntryPoint;
import com.ssafy.questory.security.config.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @DisplayName("회원가입 성공 - 201")
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
    @DisplayName("회원가입 실패 - Validation 400")
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

    @Test
    @DisplayName("로그인 성공 - 200 + Authorization 헤더 + refresh_token 쿠키")
    void member_login_success() throws Exception {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "Aa1!aaaa");

        TokenResponseDto tokens = TokenResponseDto.builder()
                .email("test@example.com")
                .accessToken("ACCESS_TOKEN_VALUE")
                .refreshToken("REFRESH_TOKEN_VALUE")
                .build();
        given(memberService.login(any(LoginRequestDto.class))).willReturn(tokens);

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer ACCESS_TOKEN_VALUE"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refresh_token=REFRESH_TOKEN_VALUE")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andDo(document("member-login",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("회원 이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token (Bearer)"),
                                headerWithName(HttpHeaders.SET_COOKIE).description("refresh_token HttpOnly 쿠키")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 실패 - Validation 400")
    void member_login_validation_fail() throws Exception {
        String invalidJson = """
                {
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(document("member-login-400",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("Access Token 재발급 성공 - 200 + Authorization 헤더")
    void member_refresh_success() throws Exception {
        given(memberService.refresh(eq("REFRESH_TOKEN_VALUE"))).willReturn("NEW_ACCESS_TOKEN_VALUE");

        mockMvc.perform(post("/api/member/refresh")
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(new jakarta.servlet.http.Cookie("refresh_token", "REFRESH_TOKEN_VALUE")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer NEW_ACCESS_TOKEN_VALUE"))
                .andDo(document("member-refresh",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("재발급된 Access Token (Bearer)")
                        )
                ));
    }

    @Test
    @DisplayName("Access Token 재발급 실패 - refresh_token 쿠키 없음 401")
    void member_refresh_fail_no_cookie() throws Exception {
        mockMvc.perform(post("/api/member/refresh")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(document("member-refresh-401-no-cookie",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("로그아웃 성공 - 200 + refresh_token 삭제 쿠키(Max-Age=0)")
    void member_logout_success() throws Exception {
        mockMvc.perform(post("/api/member/logout")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refresh_token=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andDo(document("member-logout",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseHeaders(
                                headerWithName(HttpHeaders.SET_COOKIE).description("refresh_token 삭제 쿠키")
                        )
                ));
    }
}