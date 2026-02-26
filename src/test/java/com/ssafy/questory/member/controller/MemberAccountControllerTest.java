package com.ssafy.questory.member.controller;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.response.MemberInfoResponseDto;
import com.ssafy.questory.member.service.MemberAccountService;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@NoSecurityWebMvcTest(controllers = MemberAccountController.class)
class MemberAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberAccountService memberAccountService;

    @Test
    @DisplayName("GET /api/account/me - 내 정보 조회 성공")
    void getMemberInfo_success() throws Exception {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("nickname")
                .build();

        SecurityMember principal = SecurityMember.fromMember(member);

        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        MemberInfoResponseDto response = MemberInfoResponseDto.builder()
                .email("user@example.com")
                .nickname("nickname")
                .totalExp(1234L)
                .representativeTitle("초보 모험가")
                .build();

        BDDMockito.given(memberAccountService.getUserInfo(nullable(SecurityMember.class)))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/account/me")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.nickname").value("nickname"))
                .andExpect(jsonPath("$.total_exp").value(1234))
                .andExpect(jsonPath("$.representative_title").value("초보 모험가"))
                .andDo(document("account-me-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("total_exp").description("누적 경험치"),
                                fieldWithPath("representative_title").description("대표 칭호")
                        )
                ));

        then(memberAccountService).should().getUserInfo(nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("DELETE /api/account/me - 회원 탈퇴 성공")
    void withdraw_success() throws Exception {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("nickname")
                .build();

        SecurityMember principal = SecurityMember.fromMember(member);

        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        BDDMockito.willDoNothing()
                .given(memberAccountService)
                .withdraw(org.mockito.ArgumentMatchers.nullable(SecurityMember.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.delete("/api/account/me")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("탈퇴가 완료되었습니다."))
                .andDo(document("account-withdraw-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("message").description("응답 메시지")
                        )
                ));

        then(memberAccountService).should()
                .withdraw(org.mockito.ArgumentMatchers.nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("GET /api/account?email= - 회원 검색 성공")
    void search_success() throws Exception {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("nickname")
                .build();

        SecurityMember principal = SecurityMember.fromMember(member);

        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        List<MemberInfoResponseDto> response = List.of(
                MemberInfoResponseDto.builder()
                        .email("a@example.com")
                        .nickname("A")
                        .totalExp(10L)
                        .representativeTitle("칭호A")
                        .build(),
                MemberInfoResponseDto.builder()
                        .email("ab@example.com")
                        .nickname("AB")
                        .totalExp(20L)
                        .representativeTitle("칭호AB")
                        .build()
        );

        BDDMockito.given(memberAccountService.search(nullable(SecurityMember.class), eq("a@")))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/account")
                                .principal(authentication)
                                .param("email", "a@")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[0].nickname").value("A"))
                .andExpect(jsonPath("$[0].total_exp").value(10))
                .andExpect(jsonPath("$[0].representative_title").value("칭호A"))
                .andExpect(jsonPath("$[1].email").value("ab@example.com"))
                .andExpect(jsonPath("$[1].nickname").value("AB"))
                .andExpect(jsonPath("$[1].total_exp").value(20))
                .andExpect(jsonPath("$[1].representative_title").value("칭호AB"))
                .andDo(document("account-search-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].email").description("이메일"),
                                fieldWithPath("[].nickname").description("닉네임"),
                                fieldWithPath("[].total_exp").description("누적 경험치"),
                                fieldWithPath("[].representative_title").description("대표 칭호")
                        )
                ));

        then(memberAccountService).should()
                .search(nullable(SecurityMember.class), eq("a@"));
    }

    @Test
    @DisplayName("GET /api/account - email 파라미터 누락 시 500")
    void search_missingEmailParam_500() throws Exception {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("nickname")
                .build();

        SecurityMember principal = SecurityMember.fromMember(member);

        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/account")
                                .principal(authentication)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."))
                .andDo(document("account-search-500-missing-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("message").description("에러 메시지"),
                                fieldWithPath("errors").description("상세 에러(없으면 null)").optional()
                        )
                ));
    }
}