package com.ssafy.questory.trip;

import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import com.ssafy.questory.trip.controller.TripListController;
import com.ssafy.questory.trip.dto.response.TripDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripSummaryResponseDto;
import com.ssafy.questory.trip.service.TripListService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NoSecurityWebMvcTest(controllers = TripListController.class)
class TripListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripListService tripListService;

    private UsernamePasswordAuthenticationToken authenticationToken() {
        SecurityMember principal = mock(SecurityMember.class);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of()
        );
    }

    @Test
    @DisplayName("파티별 여행 목록 조회 API")
    void getTripsByParty() throws Exception {

        List<TripSummaryResponseDto> response = List.of(
                new TripSummaryResponseDto(
                        100L,
                        "부산 2박 3일",
                        "맛집 여행",
                        LocalDate.of(2026,3,20),
                        LocalDate.of(2026,3,22),
                        1L
                ),
                new TripSummaryResponseDto(
                        101L,
                        "제주 3박 4일",
                        "관광 여행",
                        LocalDate.of(2026,4,1),
                        LocalDate.of(2026,4,4),
                        2L
                )
        );

        BDDMockito.given(tripListService.getTripsByParty(any(), eq(10L)))
                .willReturn(response);

        mockMvc.perform(get("/api/parties/{partyId}/trips", 10L)
                        .with(authentication(authenticationToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("trip-list-by-party",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("partyId").description("여행 목록을 조회할 파티 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("[].trip_id").type(JsonFieldType.NUMBER).description("여행 ID"),
                                fieldWithPath("[].title").type(JsonFieldType.STRING).description("여행 제목"),
                                fieldWithPath("[].description").type(JsonFieldType.STRING).optional().description("여행 설명"),
                                fieldWithPath("[].start_date").type(JsonFieldType.STRING).description("여행 시작일"),
                                fieldWithPath("[].end_date").type(JsonFieldType.STRING).description("여행 종료일"),
                                fieldWithPath("[].creator_id").type(JsonFieldType.NUMBER).description("여행 생성자 ID")
                        )
                ));
    }

    @Test
    @DisplayName("여행 상세 조회 API")
    void getTripDetail() throws Exception {

        TripDetailResponseDto response = new TripDetailResponseDto(
                100L,
                10L,
                1L,
                "부산 2박 3일",
                "맛집 여행",
                LocalDate.of(2026,3,20),
                LocalDate.of(2026,3,22),
                List.of()
        );

        BDDMockito.given(tripListService.getTripDetail(any(), eq(10L), eq(100L)))
                .willReturn(response);

        mockMvc.perform(get("/api/parties/{partyId}/trips/{tripId}", 10L, 100L)
                        .with(authentication(authenticationToken()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("trip-detail",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("partyId").description("파티 ID"),
                                parameterWithName("tripId").description("여행 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("trip_id").type(JsonFieldType.NUMBER).description("여행 ID"),
                                fieldWithPath("party_id").type(JsonFieldType.NUMBER).description("파티 ID"),
                                fieldWithPath("creator_id").type(JsonFieldType.NUMBER).description("생성자 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("여행 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).optional().description("여행 설명"),
                                fieldWithPath("start_date").type(JsonFieldType.STRING).description("여행 시작일"),
                                fieldWithPath("end_date").type(JsonFieldType.STRING).description("여행 종료일"),
                                subsectionWithPath("days").description("여행 일차 목록")
                        )
                ));
    }
}