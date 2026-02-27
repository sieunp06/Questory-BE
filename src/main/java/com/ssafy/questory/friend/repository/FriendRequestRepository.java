package com.ssafy.questory.friend.repository;

import com.ssafy.questory.friend.domain.FriendRequest;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.friend.dto.FriendRequestResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface FriendRequestRepository {
    List<FriendRequestResponseDto> findRequestsByMemberId(Long memberId);
    List<FriendRequestResponseDto> findSentRequestsByMemberId(Long memberId);

    Optional<FriendRequest> findPendingByIdAndReceiverId(Long friendRequestId, Long receiverId);

    boolean existsPendingRequestBetween(Long senderId, Long receiverId);

    void request(FriendRequest friendRequest);

    void updateStatus(Long friendRequestId, FriendStatus status);
}
