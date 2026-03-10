package com.ssafy.questory.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record TicketExchangeRequestDto(
        @NotBlank
        String ticket,

        @JsonProperty("code_verifier")
        String codeVerifier
) {}
