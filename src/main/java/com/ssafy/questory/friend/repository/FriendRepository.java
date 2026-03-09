package com.ssafy.questory.friend.repository;

import com.ssafy.questory.friend.dto.FriendListRawDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FriendRepository {
    List<FriendListRawDto> findFriendsRawByMemberId(Long memberId);
    List<Long> findFriendIdsAmong(Long inviterId, List<Long> targets);

    boolean existsFriend(Long senderId, Long receiverId);

    void insertFriend(Long senderId, Long receiverId);

    int deleteByFriendIdAndMemberId(Long friendId, Long memberId);
}
