package com.ssafy.questory.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.auth.dto.TicketExchangeRequestDto;
import com.ssafy.questory.auth.service.AuthService;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@NoSecurityWebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("티켓 교환에 성공하면 Authorization 헤더에 Access Token을 담아 반환한다")
    void exchange_success() throws Exception {
        TicketExchangeRequestDto request = new TicketExchangeRequestDto(
                "valid-ticket",
                "valid-code-verifier"
        );

        given(authService.exchangeTicket(request)).willReturn("access-token-value");

        mockMvc.perform(post("/api/auth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-token-value"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Access Token 발급에 성공했습니다."))
                .andDo(document("auth-exchange",
                        requestFields(
                                fieldWithPath("ticket").type(JsonFieldType.STRING)
                                        .description("OAuth 로그인 성공 후 발급된 일회용 티켓"),
                                fieldWithPath("code_verifier").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("PKCE 검증용 code_verifier 값. PKCE 적용 OAuth 로그인 시 함께 전달")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION)
                                        .description("발급된 Access Token. 형식: Bearer {accessToken}")
                        ),
                        responseFields(
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .description("요청 성공 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("응답 메시지")
                        )
                ));

        ArgumentCaptor<TicketExchangeRequestDto> captor =
                ArgumentCaptor.forClass(TicketExchangeRequestDto.class);

        then(authService).should(times(1)).exchangeTicket(captor.capture());

        TicketExchangeRequestDto captured = captor.getValue();
        assertThat(captured.ticket()).isEqualTo("valid-ticket");
        assertThat(captured.codeVerifier()).isEqualTo("valid-code-verifier");
    }

    @Test
    @DisplayName("ticket이 비어 있으면 400 Bad Request를 반환한다")
    void exchange_fail_when_ticket_is_blank() throws Exception {
        String requestBody = """
            {
              "ticket": "",
              "code_verifier": "valid-code-verifier"
            }
            """;

        mockMvc.perform(post("/api/auth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andDo(document("auth-exchange-400",
                        requestFields(
                                fieldWithPath("ticket").type(JsonFieldType.STRING)
                                        .description("OAuth 로그인 성공 후 발급된 일회용 티켓"),
                                fieldWithPath("code_verifier").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("PKCE 검증용 code_verifier 값")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("에러 메시지"),
                                fieldWithPath("errors").type(JsonFieldType.ARRAY)
                                        .description("필드 검증 에러 목록"),
                                fieldWithPath("errors[].field").type(JsonFieldType.STRING)
                                        .description("검증에 실패한 필드명"),
                                fieldWithPath("errors[].reason").type(JsonFieldType.STRING)
                                        .description("검증 실패 사유")
                        )
                ));
    }
}