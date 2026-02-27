package com.ssafy.questory.friend.repository;

import com.ssafy.questory.friend.dto.FriendListRawDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendRepository {
    List<FriendListRawDto> findFriendsRawByMemberId(Long memberId);

    boolean existsFriend(Long senderId, Long receiverId);

    void insertFriend(Long senderId, Long receiverId);
}
