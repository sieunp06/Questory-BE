package com.ssafy.questory.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.auth.dto.TicketExchangeRequestDto;
import com.ssafy.questory.auth.service.AuthService;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@NoSecurityWebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Test
    @DisplayName("exchange 성공 - 200, Authorization 헤더에 access token 반환")
    void exchange_200_restdocs() throws Exception {
        String ticket = "ticket-123";
        String accessToken = "access-token-abc";

        given(authService.exchangeTicket(ticket)).willReturn(accessToken);

        TicketExchangeRequestDto dto = new TicketExchangeRequestDto(ticket);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/auth/exchange")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(jsonPath("$.message").value("Access Token 발급에 성공했습니다."))
                .andDo(document("auth-exchange",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("ticket").description("1회용 티켓 (TicketStore에서 발급)")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer {accessToken}")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("message").description("응답 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("exchange 실패 - 400 (ticket 공백) - @NotBlank 검증")
    void exchange_400_blankTicket_restdocs() throws Exception {
        TicketExchangeRequestDto dto = new TicketExchangeRequestDto("   ");

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post("/api/auth/exchange")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andDo(document("auth-exchange-400",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("ticket").description("1회용 티켓 (공백 불가)")
                        ),
                        responseFields(
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("message").description("에러 메시지"),
                                fieldWithPath("errors").description("필드 검증 오류 목록"),
                                fieldWithPath("errors[].field").description("오류가 발생한 필드명"),
                                fieldWithPath("errors[].reason").description("오류 사유(Validation 메시지)")
                        )
                ));
    }
}