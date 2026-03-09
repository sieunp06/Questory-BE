package com.ssafy.questory.party.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record InviteResponsesDto(
        @JsonProperty("results")
        List<InviteResultDto> results
) {
    @Builder
    public record InviteResultDto(
            @JsonProperty("invitee_id") Long inviteeId,
            String result,
            String message
    ) {}
}
