package com.ssafy.questory.member.repository;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberRepository {
    Optional<Member> findById(Long memberId);
    Optional<Member> findByEmail(String email);
    Optional<LoginPrincipalRow> findLoginPrincipalByEmailWithPassword(String email);
    boolean existsByNickname(String nickname);
    int register(Member member);
}
