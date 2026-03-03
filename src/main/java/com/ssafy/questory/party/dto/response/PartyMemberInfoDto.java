package com.ssafy.questory.party.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.questory.party.domain.PartyMemberRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartyMemberInfoDto(
        @JsonProperty("member_id")
        Long memberId,
        String email,
        String nickname,
        PartyMemberRole role,

        @JsonProperty("joined_at")
        LocalDateTime joinedAt
) {}
