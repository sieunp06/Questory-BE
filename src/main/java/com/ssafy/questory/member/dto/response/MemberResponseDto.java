package com.ssafy.questory.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MemberResponseDto(
        @JsonProperty("member_id")
        Long memberId,
        String email,
        String nickname
) {}
