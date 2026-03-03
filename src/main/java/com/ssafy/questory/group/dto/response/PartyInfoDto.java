package com.ssafy.questory.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PartyInfoDto(
        @JsonProperty("party_id")
        Long partyId,
        String name,

        @JsonProperty("creator_id")
        Long creatorId,

        @JsonProperty("created_at")
        LocalDateTime createdAt
) {}
