package com.ssafy.questory.member.service;

import com.ssafy.questory.member.repository.MemberPasswordCredentialsRepository;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginFailureService {
    private final MemberRepository memberRepository;
    private final MemberPasswordCredentialsRepository memberPasswordRepository;

    private static final long LOCK_MINUTES = 10L;

    @Transactional
    public int updateFailedCount(String email, LocalDateTime now) {
        memberPasswordRepository.increaseFailedLoginCountAndUpdateLastFailedLoginAt(email, now);
        return memberPasswordRepository.findFailedCountByEmail(email);
    }

    @Transactional
    public void lockAccount(String email) {
        LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        memberRepository.changeStatusLocked(email);
        memberPasswordRepository.lock(email, lockedUntil);
    }

    @Transactional
    public void unlockAccount(String email) {
        memberRepository.changeStatusNormal(email);
        memberPasswordRepository.unlock(email);
    }
}
