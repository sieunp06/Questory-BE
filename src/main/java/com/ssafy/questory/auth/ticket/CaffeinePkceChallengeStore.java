package com.ssafy.questory.auth.ticket;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CaffeinePkceChallengeStore implements PkceChallengeStore {
    private static final long TTL_SECONDS = 120;
    private static final long MAX_SIZE = 10_000;

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(TTL_SECONDS, TimeUnit.SECONDS)
            .maximumSize(MAX_SIZE)
            .build();

    @Override
    public void save(String state, String codeChallenge) {
        cache.put(state, codeChallenge);
    }

    @Override
    public String get(String state) {
        return cache.getIfPresent(state);
    }

    @Override
    public void remove(String state) {
        cache.invalidate(state);
    }
}
