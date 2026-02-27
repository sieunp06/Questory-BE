package com.ssafy.questory.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.questory.friend.domain.FriendStatus;
import com.ssafy.questory.member.dto.response.MemberResponseDto;

public record FriendRequestResponseDto(
        @JsonProperty("friend_request_id")
        Long friendRequestId,

        @JsonProperty("sender_info")
        MemberResponseDto senderInfo,

        FriendStatus status
) {}
