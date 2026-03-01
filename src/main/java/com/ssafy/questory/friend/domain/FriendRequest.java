package com.ssafy.questory.friend.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendRequest {
    private Long friendRequestId;
    private Long senderId;
    private Long receiverId;
    private FriendStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
