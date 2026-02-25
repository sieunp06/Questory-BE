package com.ssafy.questory.member.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper
public interface MemberPasswordCredentialsRepository {
    int register(@Param("memberId") Long memberId, @Param("passwordHash") String password);

    int findFailedCountByEmail(String email);
    Optional<LocalDateTime> findLockedUntilByEmail(String email);

    void lock(String email, LocalDateTime lockedUntil);
    void unlock(String email);

    void increaseFailedLoginCountAndUpdateLastFailedLoginAt(String email, LocalDateTime now);
    void resetFailedLoginCount(String email);
}
