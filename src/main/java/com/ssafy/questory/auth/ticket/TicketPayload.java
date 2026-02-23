package com.ssafy.questory.auth.ticket;

public record TicketPayload(
        Long memberId,
        String email
) {}
