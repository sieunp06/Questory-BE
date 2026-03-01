package com.ssafy.questory.friend.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.friend.domain.FriendRequest;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.friend.dto.FriendInfoDto;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;
    private final MemberAuthPolicy memberAuthPolicy;

    public List<FriendListResponseDto> getFriends(SecurityMember securityMember) {
        Long memberId = securityMember.getMemberId();

        List<FriendListRawDto> rawList =
                friendRepository.findFriendsRawByMemberId(memberId);

        return rawList.stream()
                .map(raw -> new FriendListResponseDto(
                        raw.friendId(),
                        new FriendInfoDto(
                                raw.memberId(),
                                raw.email(),
                                raw.nickname(),
                                raw.representativeTitle(),
                                calculateLevel(raw.totalExp())
                        ),
                        raw.friendSince()
                ))
                .toList();
    }

    private int calculateLevel(Long totalExp) {
        if (totalExp == null) return 1;
        return (int) (totalExp / 1000L) + 1;
    }

    @Transactional
    public void deleteFriend(SecurityMember securityMember, Long friendId) {
        Long memberId = securityMember.getMemberId();

        Member me = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        memberAuthPolicy.validateActive(me.getStatus());

        int deleted = friendRepository.deleteByFriendIdAndMemberId(friendId, memberId);

        if (deleted == 0) {
            throw new CustomException(ErrorCode.FRIEND_NOT_FOUND);
        }
    }

    public List<FriendRequestResponseDto> getFriendRequests(SecurityMember member) {
        Long memberId = member.getMemberId();
        return friendRequestRepository.findRequestsByMemberId(memberId);
    }

    public void request(SecurityMember member, @Valid MemberEmailRequestDto dto) {
        Long senderId = member.getMemberId();
        Member receiver = memberRepository.findByEmail(dto.email())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Long receiverId = receiver.getMemberId();

        if (senderId.equals(receiverId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (receiver.getStatus() == MemberStatus.SOFT_DELETE) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }
        if (friendRepository.existsFriend(senderId, receiverId)) {
            throw new CustomException(ErrorCode.ALREADY_FRIEND);
        }
        if (friendRequestRepository.existsPendingRequestBetween(senderId, receiverId)) {
            throw new CustomException(ErrorCode.DUPLICATE_REQUEST);
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .status(FriendStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendRequestRepository.request(friendRequest);
    }

    public List<FriendRequestResponseDto> getSentFriendRequests(SecurityMember member) {
        Long memberId = member.getMemberId();
        return friendRequestRepository.findSentRequestsByMemberId(memberId);
    }

    @Transactional
    public void acceptRequest(SecurityMember member, Long friendRequestId) {
        Long receiverId = member.getMemberId();

        FriendRequest pending = friendRequestRepository.findPendingByIdAndReceiverId(friendRequestId, receiverId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        Long senderId = pending.getSenderId();

        if (memberRepository.findStatusById(senderId) == MemberStatus.SOFT_DELETE) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }
        if (memberRepository.findStatusById(receiverId) == MemberStatus.SOFT_DELETE) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }

        int updated = friendRequestRepository.updateStatusIfPendingByReceiver(
                friendRequestId, receiverId, FriendStatus.ACCEPTED
        );

        if (updated == 0) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        try {
            friendRepository.insertFriend(senderId, receiverId);
        } catch (DataIntegrityViolationException e) {}
    }

    @Transactional
    public void rejectRequest(SecurityMember member, Long friendRequestId) {
        Long receiverId = member.getMemberId();

        int updated = friendRequestRepository.updateStatusIfPendingByReceiver(
                friendRequestId, receiverId, FriendStatus.REJECTED
        );

        if (updated == 0) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }

    @Transactional
    public void cancelRequest(SecurityMember member, Long friendRequestId) {
        Long senderId = member.getMemberId();

        int updated = friendRequestRepository.updateStatusIfPendingBySender(
                friendRequestId, senderId, FriendStatus.CANCELED
        );

        if (updated == 0) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
    }
}
