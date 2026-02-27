package com.ssafy.questory.friend.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendRepository {
    boolean existsFriend(Long senderId, Long receiverId);

    void insertFriend(Long senderId, Long receiverId);
}
