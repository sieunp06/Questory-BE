package com.ssafy.questory.party.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InviteRequestDto(
        @NotEmpty
        @JsonProperty("invitee_ids")
        List<Long> inviteeIds
) {}
