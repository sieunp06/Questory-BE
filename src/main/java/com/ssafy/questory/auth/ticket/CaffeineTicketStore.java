package com.ssafy.questory.auth.ticket;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CaffeineTicketStore implements TicketStore {
    private static final long TTL_SECONDS = 60;
    private static final long MAX_SIZE = 10_000;

    private final Cache<String, TicketPayload> cache =
            Caffeine.newBuilder()
                    .expireAfterWrite(TTL_SECONDS, TimeUnit.SECONDS)
                    .maximumSize(MAX_SIZE)
                    .build();

    @Override
    public String issue(TicketPayload payload) {
        String ticket = UUID.randomUUID().toString();
        cache.put(ticket, payload);
        return ticket;
    }

    @Override
    public TicketPayload consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }
        TicketPayload payload = cache.getIfPresent(ticket);
        if (payload != null) {
            cache.invalidate(ticket);
        }
        return payload;
    }
}
