package com.ssafy.questory.friend.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Friend {
    private Long friendId;
    private Long memberAId;
    private Long memberBId;
    private LocalDateTime createdAt;
}
