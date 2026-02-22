package com.ssafy.questory.auth.ticket;

public interface TicketStore {
    String issue(TicketPayload payload);
    TicketPayload consume(String ticket);
}
