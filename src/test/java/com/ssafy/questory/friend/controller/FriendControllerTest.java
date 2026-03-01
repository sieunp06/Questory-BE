package com.ssafy.questory.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.friend.dto.FriendListResponseDto;
import com.ssafy.questory.friend.dto.FriendRequestResponseDto;
import com.ssafy.questory.friend.service.FriendService;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.support.NoSecurityWebMvcTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NoSecurityWebMvcTest(controllers = FriendController.class)
class FriendControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean FriendService friendService;

    private Authentication authPrincipal() {
        SecurityMember principal = BDDMockito.mock(SecurityMember.class);
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }

    private static FieldDescriptor[] apiResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("success").description("요청 성공 여부"),
                fieldWithPath("message").description("응답 메시지")
        };
    }

    private static FieldDescriptor[] errorResponseFields_withErrors() {
        return new FieldDescriptor[]{
                fieldWithPath("status").description("HTTP 상태 코드"),
                fieldWithPath("message").description("에러 메시지"),
                fieldWithPath("errors").description("필드 에러 목록"),
                fieldWithPath("errors[].field").description("에러 필드명"),
                fieldWithPath("errors[].reason").description("에러 사유")
        };
    }

    private static FieldDescriptor[] errorResponseFields_noErrors() {
        return new FieldDescriptor[]{
                fieldWithPath("status").description("HTTP 상태 코드"),
                fieldWithPath("message").description("에러 메시지"),
                fieldWithPath("errors").description("필드 에러 목록(null 가능)")
        };
    }

    private static FieldDescriptor[] friendRequestResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("[].friend_request_id").description("친구 요청 ID"),
                fieldWithPath("[].sender_info").description("요청 보낸 회원 정보"),
                fieldWithPath("[].sender_info.member_id").description("요청 보낸 회원 ID"),
                fieldWithPath("[].sender_info.email").description("요청 보낸 회원 이메일"),
                fieldWithPath("[].sender_info.nickname").description("요청 보낸 회원 닉네임"),
                fieldWithPath("[].status").description("친구 요청 상태")
        };
    }

    @Test
    @DisplayName("친구 목록 조회 - 성공 (200)")
    void getFriends_success() throws Exception {
        given(friendService.getFriends(any()))
                .willReturn(List.of(BDDMockito.mock(FriendListResponseDto.class)));

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/friend")
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-get-friends",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                subsectionWithPath("[]").description("친구 목록")
                        )
                ));

        then(friendService).should().getFriends(nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("친구 삭제 - 성공 (200)")
    void deleteFriend_success() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/friend/{friendId}", 10L)
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-delete-friend",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("friendId").description("삭제할 친구의 회원 ID")
                        ),
                        responseFields(apiResponseFields())
                ));

        then(friendService).should().deleteFriend(nullable(SecurityMember.class), eq(10L));
    }

    @Test
    @DisplayName("받은 친구 요청 목록 조회 - 성공 (200)")
    void getFriendRequestInfo_success() throws Exception {
        FriendRequestResponseDto dto = new FriendRequestResponseDto(
                1L,
                MemberResponseDto.builder()
                        .memberId(2L)
                        .email("sender@example.com")
                        .nickname("sender")
                        .build(),
                FriendStatus.PENDING
        );

        given(friendService.getFriendRequests(any()))
                .willReturn(List.of(dto));

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/friend/request")
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-get-received-requests",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(friendRequestResponseFields())
                ));

        then(friendService).should().getFriendRequests(nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("친구 요청 보내기 - 성공 (201)")
    void request_success() throws Exception {
        MemberEmailRequestDto dto = new MemberEmailRequestDto("target@example.com");

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request")
                        .with(authentication(authPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andDo(document("friend-request-send",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("친구 요청을 보낼 대상 회원 이메일")
                        ),
                        responseFields(apiResponseFields())
                ));

        then(friendService).should().request(nullable(SecurityMember.class), any(MemberEmailRequestDto.class));
    }

    @Test
    @DisplayName("친구 요청 보내기 - Validation 실패 (400): email blank")
    void request_validation_fail_blank_email() throws Exception {
        String body = """
                {"email":""}
                """;

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request")
                        .with(authentication(authPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andDo(document("friend-request-send-400-blank-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("친구 요청을 보낼 대상 회원 이메일 (필수, 이메일 형식)")
                        ),
                        responseFields(errorResponseFields_withErrors())
                ));

        then(friendService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("친구 요청 보내기 - Validation 실패 (400): email invalid")
    void request_validation_fail_invalid_email() throws Exception {
        String body = """
                {"email":"not-an-email"}
                """;

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request")
                        .with(authentication(authPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andDo(document("friend-request-send-400-invalid-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").description("친구 요청을 보낼 대상 회원 이메일 (필수, 이메일 형식)")
                        ),
                        responseFields(errorResponseFields_withErrors())
                ));

        then(friendService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("친구 요청 보내기 - JSON 파싱 실패 (400)")
    void request_not_readable_400() throws Exception {
        String brokenJson = """
                {"email": "test@example.com"
                """;

        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request")
                        .with(authentication(authPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andDo(document("friend-request-send-400-not-readable",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(errorResponseFields_noErrors())
                ));

        then(friendService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("보낸 친구 요청 목록 조회 - 성공 (200)")
    void getSentFriendRequests_success() throws Exception {
        FriendRequestResponseDto dto = new FriendRequestResponseDto(
                10L,
                MemberResponseDto.builder()
                        .memberId(1L)
                        .email("me@example.com")
                        .nickname("me")
                        .build(),
                FriendStatus.PENDING
        );

        given(friendService.getSentFriendRequests(any()))
                .willReturn(List.of(dto));

        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/friend/request/sent")
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-get-sent-requests",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(friendRequestResponseFields())
                ));

        then(friendService).should().getSentFriendRequests(nullable(SecurityMember.class));
    }

    @Test
    @DisplayName("친구 요청 수락 - 성공 (200)")
    void accept_success() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request/{friendRequestId}/accept", 99L)
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-request-accept",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("friendRequestId").description("수락할 친구 요청 ID")
                        ),
                        responseFields(apiResponseFields())
                ));

        then(friendService).should().acceptRequest(nullable(SecurityMember.class), eq(99L));
    }

    @Test
    @DisplayName("친구 요청 거절 - 성공 (200)")
    void reject_success() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request/{friendRequestId}/reject", 100L)
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-request-reject",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("friendRequestId").description("거절할 친구 요청 ID")
                        ),
                        responseFields(apiResponseFields())
                ));

        then(friendService).should().rejectRequest(nullable(SecurityMember.class), eq(100L));
    }

    @Test
    @DisplayName("친구 요청 취소 - 성공 (200)")
    void cancel_success() throws Exception {
        mockMvc.perform(RestDocumentationRequestBuilders.post("/api/friend/request/{friendRequestId}/cancel", 101L)
                        .with(authentication(authPrincipal())))
                .andExpect(status().isOk())
                .andDo(document("friend-request-cancel",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("friendRequestId").description("취소할 친구 요청 ID")
                        ),
                        responseFields(apiResponseFields())
                ));

        then(friendService).should().cancelRequest(nullable(SecurityMember.class), eq(101L));
    }
}