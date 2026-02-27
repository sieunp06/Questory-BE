package com.ssafy.questory.friend.dto;

import java.time.LocalDateTime;

public record FriendListRawDto(
        Long friendId,
        Long memberId,
        String email,
        String nickname,
        String representativeTitle,
        Long totalExp,
        LocalDateTime friendSince
) {}