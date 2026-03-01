package com.ssafy.questory.friend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FriendInfoDto(
        @JsonProperty("member_id")
        Long memberId,
        String email,
        String nickname,

        @JsonProperty("representative_title")
        String representativeTitle,

        int level
) {}