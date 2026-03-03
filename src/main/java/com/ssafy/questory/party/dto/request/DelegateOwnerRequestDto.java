package com.ssafy.questory.party.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record DelegateOwnerRequestDto(
        @NotNull
        @JsonProperty("new_owner_id")
        Long newOwnerId
) {}
