package com.ssafy.questory.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record FriendListResponseDto(
        @JsonProperty("friend_id")
        Long friendId,

        @JsonProperty("friend_info")
        FriendInfoDto friendInfo,

        @JsonProperty("friend_since")
        LocalDateTime friendSince
) {}
