package com.ssafy.questory.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import com.ssafy.questory.trip.dto.request.CreateRequestDto;
import com.ssafy.questory.trip.dto.request.UpdateRequestDto;
import com.ssafy.questory.trip.dto.response.CreateResponseDto;
import com.ssafy.questory.trip.service.TripService;
import com.ssafy.questory.trip.service.TripUpdateService;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NoSecurityWebMvcTest(controllers = TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private TripUpdateService tripUpdateService;

    private UsernamePasswordAuthenticationToken authenticationToken() {
        SecurityMember principal = mock(SecurityMember.class);
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of()
        );
    }

    @Test
    @DisplayName("여행 생성 API")
    void createTrip() throws Exception {
        CreateRequestDto request = new CreateRequestDto(
                10L,
                "부산 2박 3일",
                "맛집 위주 여행",
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 22)
        );

        CreateResponseDto response = CreateResponseDto.builder()
                .tripId(100L)
                .title("부산 2박 3일")
                .description("맛집 위주 여행")
                .startDate(LocalDate.of(2026, 3, 20))
                .endDate(LocalDate.of(2026, 3, 22))
                .build();

        BDDMockito.given(tripService.create(any(), any(CreateRequestDto.class)))
                .willReturn(response);

        mockMvc.perform(post("/api/trip")
                        .with(authentication(authenticationToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trip_id").value(100L))
                .andExpect(jsonPath("$.title").value("부산 2박 3일"))
                .andExpect(jsonPath("$.description").value("맛집 위주 여행"))
                .andExpect(jsonPath("$.start_date").value("2026-03-20"))
                .andExpect(jsonPath("$.end_date").value("2026-03-22"))
                .andDo(document("trip-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("party_id").type(JsonFieldType.NUMBER).description("여행이 속할 파티 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("여행 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).optional().description("여행 설명"),
                                fieldWithPath("start_date").type(JsonFieldType.STRING).description("여행 시작일 (yyyy-MM-dd)"),
                                fieldWithPath("end_date").type(JsonFieldType.STRING).description("여행 종료일 (yyyy-MM-dd)")
                        ),
                        responseFields(
                                fieldWithPath("trip_id").type(JsonFieldType.NUMBER).description("생성된 여행 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("여행 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).optional().description("여행 설명"),
                                fieldWithPath("start_date").type(JsonFieldType.STRING).description("여행 시작일"),
                                fieldWithPath("end_date").type(JsonFieldType.STRING).description("여행 종료일")
                        )
                ));
    }

    @Test
    @DisplayName("여행 기본 정보 수정 API")
    void updateTripInfo() throws Exception {
        UpdateRequestDto request = new UpdateRequestDto(
                "부산 1박 2일 수정",
                "일정 축소",
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 21)
        );

        mockMvc.perform(patch("/api/trip/{tripId}", 100L)
                        .with(authentication(authenticationToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("여행 기본 정보 수정에 성공했습니다."))
                .andDo(document("trip-update-info",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("tripId").description("수정할 여행 ID")
                        ),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("수정할 여행 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).optional().description("수정할 여행 설명"),
                                fieldWithPath("start_date").type(JsonFieldType.STRING).description("수정할 시작일 (yyyy-MM-dd)"),
                                fieldWithPath("end_date").type(JsonFieldType.STRING).description("수정할 종료일 (yyyy-MM-dd)")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        BDDMockito.then(tripUpdateService)
                .should()
                .updateInfo(any(), eq(100L), any(UpdateRequestDto.class));
    }

    @Test
    @DisplayName("여행 삭제 API")
    void deleteTrip() throws Exception {
        mockMvc.perform(delete("/api/trip/{tripId}", 100L)
                        .with(authentication(authenticationToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("여행 삭제에 성공했습니다."))
                .andDo(document("trip-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("tripId").description("삭제할 여행 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));

        BDDMockito.then(tripService)
                .should()
                .delete(any(), eq(100L));
    }

    @Test
    @DisplayName("여행 생성 검증 실패 - 제목 공백")
    void createTrip_validationFail_blankTitle() throws Exception {
        CreateRequestDto request = new CreateRequestDto(
                10L,
                "",
                "설명",
                LocalDate.of(2026, 3, 20),
                LocalDate.of(2026, 3, 22)
        );

        mockMvc.perform(post("/api/trip")
                        .with(authentication(authenticationToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여행 수정 검증 실패 - 시작일 null")
    void updateTrip_validationFail_nullStartDate() throws Exception {
        String request = """
                {
                  "title": "수정 제목",
                  "description": "수정 설명",
                  "start_date": null,
                  "end_date": "2026-03-21"
                }
                """;

        mockMvc.perform(patch("/api/trip/{tripId}", 100L)
                        .with(authentication(authenticationToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}