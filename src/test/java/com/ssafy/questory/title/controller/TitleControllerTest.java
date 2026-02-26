package com.ssafy.questory.title.controller;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import com.ssafy.questory.title.dto.request.AcquireTitleRequestDto;
import com.ssafy.questory.title.dto.request.UpdateRepresentativeTitleRequestDto;
import com.ssafy.questory.title.dto.response.TitleResponseDto;
import com.ssafy.questory.title.service.TitleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@NoSecurityWebMvcTest(controllers = TitleController.class)
class TitleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TitleService titleService;

    private UsernamePasswordAuthenticationToken authentication() {
        Member member = Member.builder()
                .email("user@example.com")
                .nickname("nickname")
                .build();
        SecurityMember principal = SecurityMember.fromMember(member);

        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    @Test
    @DisplayName("GET /api/account/title - 내 칭호 목록 조회 성공")
    void getMyTitles_success() throws Exception {
        var auth = authentication();

        List<TitleResponseDto> response = List.of(
                TitleResponseDto.builder()
                        .titleId(1L)
                        .name("초보 모험가")
                        .acquiredAt(java.time.LocalDateTime.of(2024, 1, 1, 10, 0))
                        .representative(true)
                        .build(),
                TitleResponseDto.builder()
                        .titleId(2L)
                        .name("열심히 하는 사람")
                        .acquiredAt(java.time.LocalDateTime.of(2024, 1, 2, 10, 0))
                        .representative(false)
                        .build()
        );

        BDDMockito.given(titleService.getMyTitles(nullable(SecurityMember.class)))
                .willReturn(response);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.get("/api/account/title")
                                .principal(auth)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titleId").value(1))
                .andExpect(jsonPath("$[0].name").value("초보 모험가"))
                .andExpect(jsonPath("$[0].representative").value(true))
                .andDo(document("title-get-my-titles-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("[].titleId").description("칭호 ID"),
                                fieldWithPath("[].name").description("칭호 이름"),
                                fieldWithPath("[].acquiredAt").description("칭호 획득 시각 (ISO-8601)"),
                                fieldWithPath("[].representative").description("대표 칭호 여부")
                        )
                ));

        then(titleService).should().getMyTitles(nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("POST /api/account/title - 칭호 획득 성공")
    void acquireTitle_success() throws Exception {
        var auth = authentication();

        String requestBody = """
                {
                  "titleId": 1
                }
                """;

        BDDMockito.willDoNothing()
                .given(titleService)
                .acquireTitle(nullable(SecurityMember.class), any(AcquireTitleRequestDto.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/account/title")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("칭호가 추가되었습니다.ㅊ"))
                .andDo(document("title-acquire-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("titleId").description("획득할 칭호 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("message").description("응답 메시지")
                        )
                ));

        then(titleService).should()
                .acquireTitle(nullable(SecurityMember.class), any(AcquireTitleRequestDto.class));
    }

    @Test
    @DisplayName("PATCH /api/account/title - 대표 칭호 변경 성공")
    void updateRepresentativeTitle_success() throws Exception {
        var auth = authentication();

        String requestBody = """
                {
                  "titleId": 2
                }
                """;

        BDDMockito.willDoNothing()
                .given(titleService)
                .updateRepresentativeTitle(nullable(SecurityMember.class), any(UpdateRepresentativeTitleRequestDto.class));

        mockMvc.perform(
                        RestDocumentationRequestBuilders.patch("/api/account/title")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대표 칭호 변경이 완료되었습니다."))
                .andDo(document("title-update-representative-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("titleId").description("대표로 설정할 칭호 ID")
                        ),
                        responseFields(
                                fieldWithPath("success").description("요청 성공 여부"),
                                fieldWithPath("message").description("응답 메시지")
                        )
                ));

        then(titleService).should()
                .updateRepresentativeTitle(nullable(SecurityMember.class), any(UpdateRepresentativeTitleRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/account/title - 칭호 획득 실패 (Validation)")
    void acquireTitle_validationFail_400() throws Exception {
        var auth = authentication();

        String requestBody = """
                {
                }
                """;

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/account/title")
                                .principal(auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andDo(document("title-acquire-400-validation",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }
}