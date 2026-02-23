package com.ssafy.questory.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketExchangeRequestDto(@NotBlank String ticket) {
}
