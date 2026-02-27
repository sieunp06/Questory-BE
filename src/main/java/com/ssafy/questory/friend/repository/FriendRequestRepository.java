package com.ssafy.questory.friend.repository;

import com.ssafy.questory.friend.domain.FriendRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendRequestRepository {
    boolean existsPendingRequestBetween(Long senderId, Long receiverId);

    void request(FriendRequest friendRequest);
}
