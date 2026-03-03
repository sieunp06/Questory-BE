package com.ssafy.questory.party.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssafy.questory.party.domain.PartyMemberRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartyInfoDto(
        @JsonProperty("party_id")
        Long partyId,
        String name,

        @JsonProperty("creator_id")
        Long creatorId,

        PartyMemberRole role,

        @JsonProperty("joined_at")
        LocalDateTime joinedAt,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}
