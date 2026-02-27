package com.ssafy.questory.friend.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.friend.domain.FriendRequest;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.friend.dto.FriendRequestResponseDto;
import com.ssafy.questory.friend.repository.FriendRepository;
import com.ssafy.questory.friend.repository.FriendRequestRepository;
import com.ssafy.questory.mail.dto.request.MemberEmailRequestDto;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final MemberRepository memberRepository;

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
}
