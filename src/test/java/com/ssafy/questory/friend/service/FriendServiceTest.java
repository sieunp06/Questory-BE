package com.ssafy.questory.friend.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.friend.domain.FriendRequest;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.friend.dto.FriendListRawDto;
import com.ssafy.questory.friend.dto.FriendListResponseDto;
import com.ssafy.questory.friend.dto.FriendRequestResponseDto;
import com.ssafy.questory.friend.repository.FriendRepository;
import com.ssafy.questory.friend.repository.FriendRequestRepository;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.security.config.MemberAuthPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {
    @Mock FriendRepository friendRepository;
    @Mock FriendRequestRepository friendRequestRepository;
    @Mock MemberRepository memberRepository;
    @Mock MemberAuthPolicy memberAuthPolicy;

    @InjectMocks
    FriendService friendService;

    private SecurityMember securityMemberWithId(long memberId) {
        SecurityMember sm = mock(SecurityMember.class);
        given(sm.getMemberId()).willReturn(memberId);
        return sm;
    }

    private Member member(Long memberId, String email, MemberStatus status) {
        Member m = Member.builder()
                .email(email)
                .nickname("nick")
                .build();
        ReflectionTestUtils.setField(m, "memberId", memberId);
        ReflectionTestUtils.setField(m, "status", status);
        return m;
    }

    private FriendRequest pendingRequest(Long senderId, Long receiverId) {
        return FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getFriends: raw 목록을 FriendListResponseDto로 매핑한다 (level 계산 포함)")
    void getFriends_maps_raw_to_response() {
        SecurityMember sm = securityMemberWithId(1L);

        FriendListRawDto raw1 = new FriendListRawDto(
                10L, 2L, "a@a.com", "nickA", "TITLE", 0L,
                LocalDateTime.of(2026, 1, 1, 0, 0)
        );
        FriendListRawDto raw2 = new FriendListRawDto(
                11L, 3L, "b@b.com", "nickB", null, 2500L,
                LocalDateTime.of(2026, 1, 2, 0, 0)
        );

        given(friendRepository.findFriendsRawByMemberId(1L)).willReturn(List.of(raw1, raw2));

        List<FriendListResponseDto> result = friendService.getFriends(sm);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).friendId()).isEqualTo(10L);
        assertThat(result.get(0).friendInfo().level()).isEqualTo(1);
        assertThat(result.get(1).friendInfo().level()).isEqualTo(3);

        then(friendRepository).should().findFriendsRawByMemberId(1L);
    }

    @Nested
    class DeleteFriendTests {
        @Test
        @DisplayName("deleteFriend: 정상 삭제 (deleted=1)")
        void deleteFriend_success() {
            SecurityMember sm = securityMemberWithId(1L);
            Member me = member(1L, "me@me.com", MemberStatus.NORMAL);

            given(memberRepository.findById(1L)).willReturn(Optional.of(me));
            given(friendRepository.deleteByFriendIdAndMemberId(10L, 1L)).willReturn(1);

            friendService.deleteFriend(sm, 10L);

            then(memberRepository).should().findById(1L);
            then(memberAuthPolicy).should().validateActive(MemberStatus.NORMAL);
            then(friendRepository).should().deleteByFriendIdAndMemberId(10L, 1L);
        }

        @Test
        @DisplayName("deleteFriend: 내 회원이 없으면 MEMBER_NOT_FOUND")
        void deleteFriend_member_not_found() {
            SecurityMember sm = securityMemberWithId(1L);
            given(memberRepository.findById(1L)).willReturn(Optional.empty());

            CustomException ex = catchThrowableOfType(
                    () -> friendService.deleteFriend(sm, 10L),
                    CustomException.class
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
            then(friendRepository).shouldHaveNoInteractions();
            then(memberAuthPolicy).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("deleteFriend: 삭제 대상이 없으면 FRIEND_NOT_FOUND")
        void deleteFriend_friend_not_found() {
            SecurityMember sm = securityMemberWithId(1L);
            Member me = member(1L, "me@me.com", MemberStatus.NORMAL);

            given(memberRepository.findById(1L)).willReturn(Optional.of(me));
            given(friendRepository.deleteByFriendIdAndMemberId(10L, 1L)).willReturn(0);

            CustomException ex = catchThrowableOfType(
                    () -> friendService.deleteFriend(sm, 10L),
                    CustomException.class
            );

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FRIEND_NOT_FOUND);
        }
    }

    @Test
    @DisplayName("getFriendRequests: repository 결과를 그대로 반환")
    void getFriendRequests_returns_repo_result() {
        SecurityMember sm = securityMemberWithId(1L);

        FriendRequestResponseDto dto = mock(FriendRequestResponseDto.class);
        given(friendRequestRepository.findRequestsByMemberId(1L)).willReturn(List.of(dto));

        List<FriendRequestResponseDto> result = friendService.getFriendRequests(sm);

        assertThat(result).containsExactly(dto);
        then(friendRequestRepository).should().findRequestsByMemberId(1L);
    }

    @Test
    @DisplayName("getSentFriendRequests: repository 결과를 그대로 반환")
    void getSentFriendRequests_returns_repo_result() {
        SecurityMember sm = securityMemberWithId(1L);

        FriendRequestResponseDto dto = mock(FriendRequestResponseDto.class);
        given(friendRequestRepository.findSentRequestsByMemberId(1L)).willReturn(List.of(dto));

        List<FriendRequestResponseDto> result = friendService.getSentFriendRequests(sm);

        assertThat(result).containsExactly(dto);
        then(friendRequestRepository).should().findSentRequestsByMemberId(1L);
    }

    @Nested
    class RequestTests {

        @Test
        @DisplayName("request: 정상 요청이면 FriendRequest 생성 후 repository.request 호출")
        void request_success() {
            SecurityMember sm = securityMemberWithId(1L);
            Member receiver = member(2L, "target@example.com", MemberStatus.NORMAL);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("target@example.com");

            given(memberRepository.findByEmail("target@example.com")).willReturn(Optional.of(receiver));
            given(friendRepository.existsFriend(1L, 2L)).willReturn(false);
            given(friendRequestRepository.existsPendingRequestBetween(1L, 2L)).willReturn(false);

            friendService.request(sm, dto);

            then(friendRequestRepository).should().request(argThat(fr ->
                    fr.getSenderId().equals(1L)
                            && fr.getReceiverId().equals(2L)
                            && fr.getStatus() == FriendStatus.PENDING
                            && fr.getCreatedAt() != null
            ));
        }

        @Test
        @DisplayName("request: 수신자가 없으면 MEMBER_NOT_FOUND")
        void request_receiver_not_found() {
            SecurityMember sm = securityMemberWithId(1L);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("x@x.com");
            given(memberRepository.findByEmail("x@x.com")).willReturn(Optional.empty());

            CustomException ex = catchThrowableOfType(() -> friendService.request(sm, dto), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
            then(friendRepository).shouldHaveNoInteractions();
            then(friendRequestRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("request: 자기 자신에게 요청하면 INVALID_REQUEST")
        void request_to_self_invalid() {
            SecurityMember sm = securityMemberWithId(1L);
            Member receiver = member(1L, "me@me.com", MemberStatus.NORMAL);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("me@me.com");

            given(memberRepository.findByEmail("me@me.com")).willReturn(Optional.of(receiver));

            CustomException ex = catchThrowableOfType(() -> friendService.request(sm, dto), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
            then(friendRepository).shouldHaveNoInteractions();
            then(friendRequestRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("request: 수신자가 SOFT_DELETE면 MEMBER_DELETED")
        void request_receiver_deleted() {
            SecurityMember sm = securityMemberWithId(1L);
            Member receiver = member(2L, "target@example.com", MemberStatus.SOFT_DELETE);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("target@example.com");

            given(memberRepository.findByEmail("target@example.com")).willReturn(Optional.of(receiver));

            CustomException ex = catchThrowableOfType(() -> friendService.request(sm, dto), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);
            then(friendRepository).shouldHaveNoInteractions();
            then(friendRequestRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("request: 이미 친구면 ALREADY_FRIEND")
        void request_already_friend() {
            SecurityMember sm = securityMemberWithId(1L);
            Member receiver = member(2L, "target@example.com", MemberStatus.NORMAL);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("target@example.com");

            given(memberRepository.findByEmail("target@example.com")).willReturn(Optional.of(receiver));
            given(friendRepository.existsFriend(1L, 2L)).willReturn(true);

            CustomException ex = catchThrowableOfType(() -> friendService.request(sm, dto), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ALREADY_FRIEND);
            then(friendRequestRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("request: 이미 pending 요청이 있으면 DUPLICATE_REQUEST")
        void request_duplicate_pending() {
            SecurityMember sm = securityMemberWithId(1L);
            Member receiver = member(2L, "target@example.com", MemberStatus.NORMAL);
            MemberEmailRequestDto dto = new MemberEmailRequestDto("target@example.com");

            given(memberRepository.findByEmail("target@example.com")).willReturn(Optional.of(receiver));
            given(friendRepository.existsFriend(1L, 2L)).willReturn(false);
            given(friendRequestRepository.existsPendingRequestBetween(1L, 2L)).willReturn(true);

            CustomException ex = catchThrowableOfType(() -> friendService.request(sm, dto), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_REQUEST);
            then(friendRequestRepository).should(never()).request(any());
        }
    }

    @Nested
    class AcceptRequestTests {

        @Test
        @DisplayName("acceptRequest: 정상 수락 - status 업데이트 후 친구 insert")
        void accept_success() {
            SecurityMember sm = securityMemberWithId(2L);
            FriendRequest pending = pendingRequest(1L, 2L);

            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.of(pending));

            given(memberRepository.findStatusById(1L)).willReturn(MemberStatus.NORMAL);
            given(memberRepository.findStatusById(2L)).willReturn(MemberStatus.NORMAL);

            given(friendRequestRepository.updateStatusIfPendingByReceiver(100L, 2L, FriendStatus.ACCEPTED))
                    .willReturn(1);

            friendService.acceptRequest(sm, 100L);

            then(friendRequestRepository).should().updateStatusIfPendingByReceiver(100L, 2L, FriendStatus.ACCEPTED);
            then(friendRepository).should().insertFriend(1L, 2L);
        }

        @Test
        @DisplayName("acceptRequest: pending 요청이 없으면 FRIEND_REQUEST_NOT_FOUND")
        void accept_not_found_when_pending_missing() {
            SecurityMember sm = securityMemberWithId(2L);
            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.empty());

            CustomException ex = catchThrowableOfType(() -> friendService.acceptRequest(sm, 100L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
            then(friendRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("acceptRequest: sender가 SOFT_DELETE면 MEMBER_DELETED")
        void accept_sender_deleted() {
            SecurityMember sm = securityMemberWithId(2L);
            FriendRequest pending = pendingRequest(1L, 2L);

            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.of(pending));

            given(memberRepository.findStatusById(1L)).willReturn(MemberStatus.SOFT_DELETE);

            CustomException ex = catchThrowableOfType(() -> friendService.acceptRequest(sm, 100L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);
            then(friendRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("acceptRequest: receiver가 SOFT_DELETE면 MEMBER_DELETED")
        void accept_receiver_deleted() {
            SecurityMember sm = securityMemberWithId(2L);
            FriendRequest pending = pendingRequest(1L, 2L);

            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.of(pending));

            given(memberRepository.findStatusById(1L)).willReturn(MemberStatus.NORMAL);
            given(memberRepository.findStatusById(2L)).willReturn(MemberStatus.SOFT_DELETE);

            CustomException ex = catchThrowableOfType(() -> friendService.acceptRequest(sm, 100L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);
            then(friendRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("acceptRequest: update가 0이면 FRIEND_REQUEST_NOT_FOUND")
        void accept_update_zero_not_found() {
            SecurityMember sm = securityMemberWithId(2L);
            FriendRequest pending = pendingRequest(1L, 2L);

            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.of(pending));

            given(memberRepository.findStatusById(1L)).willReturn(MemberStatus.NORMAL);
            given(memberRepository.findStatusById(2L)).willReturn(MemberStatus.NORMAL);

            given(friendRequestRepository.updateStatusIfPendingByReceiver(100L, 2L, FriendStatus.ACCEPTED))
                    .willReturn(0);

            CustomException ex = catchThrowableOfType(() -> friendService.acceptRequest(sm, 100L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
            then(friendRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("acceptRequest: insertFriend에서 DataIntegrityViolationException이 나도 무시한다")
        void accept_ignores_duplicate_insert_exception() {
            SecurityMember sm = securityMemberWithId(2L);
            FriendRequest pending = pendingRequest(1L, 2L);

            given(friendRequestRepository.findPendingByIdAndReceiverId(100L, 2L))
                    .willReturn(Optional.of(pending));

            given(memberRepository.findStatusById(1L)).willReturn(MemberStatus.NORMAL);
            given(memberRepository.findStatusById(2L)).willReturn(MemberStatus.NORMAL);

            given(friendRequestRepository.updateStatusIfPendingByReceiver(100L, 2L, FriendStatus.ACCEPTED))
                    .willReturn(1);

            willThrow(new DataIntegrityViolationException("dup"))
                    .given(friendRepository).insertFriend(1L, 2L);

            assertThatCode(() -> friendService.acceptRequest(sm, 100L))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class RejectRequestTests {

        @Test
        @DisplayName("rejectRequest: update 성공이면 예외 없음")
        void reject_success() {
            SecurityMember sm = securityMemberWithId(2L);

            given(friendRequestRepository.updateStatusIfPendingByReceiver(10L, 2L, FriendStatus.REJECTED))
                    .willReturn(1);

            friendService.rejectRequest(sm, 10L);

            then(friendRequestRepository).should().updateStatusIfPendingByReceiver(10L, 2L, FriendStatus.REJECTED);
        }

        @Test
        @DisplayName("rejectRequest: update=0이면 FRIEND_REQUEST_NOT_FOUND")
        void reject_not_found() {
            SecurityMember sm = securityMemberWithId(2L);

            given(friendRequestRepository.updateStatusIfPendingByReceiver(10L, 2L, FriendStatus.REJECTED))
                    .willReturn(0);

            CustomException ex = catchThrowableOfType(() -> friendService.rejectRequest(sm, 10L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }

    @Nested
    class CancelRequestTests {

        @Test
        @DisplayName("cancelRequest: update 성공이면 예외 없음")
        void cancel_success() {
            SecurityMember sm = securityMemberWithId(1L);

            given(friendRequestRepository.updateStatusIfPendingBySender(10L, 1L, FriendStatus.CANCELED))
                    .willReturn(1);

            friendService.cancelRequest(sm, 10L);

            then(friendRequestRepository).should().updateStatusIfPendingBySender(10L, 1L, FriendStatus.CANCELED);
        }

        @Test
        @DisplayName("cancelRequest: update=0이면 FRIEND_REQUEST_NOT_FOUND")
        void cancel_not_found() {
            SecurityMember sm = securityMemberWithId(1L);

            given(friendRequestRepository.updateStatusIfPendingBySender(10L, 1L, FriendStatus.CANCELED))
                    .willReturn(0);

            CustomException ex = catchThrowableOfType(() -> friendService.cancelRequest(sm, 10L), CustomException.class);

            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }
}