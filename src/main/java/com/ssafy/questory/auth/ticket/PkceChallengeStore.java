package com.ssafy.questory.auth.ticket;

public interface PkceChallengeStore {
    void save(String state, String codeChallenge);
    String get(String state);
    void remove(String state);
}
