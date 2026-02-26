package com.ssafy.questory.member.repository;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.dto.response.MemberInfoResponseDto;
import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberRepository {
    Optional<Member> findById(Long memberId);
    Optional<Member> findByEmail(String email);
    MemberStatus findStatusById(Long memberId);
    Optional<LoginPrincipalRow> findLoginPrincipalByEmailWithPassword(String email);
    Optional<MemberInfoResponseDto> findMyInfo(Long memberId);

    List<MemberInfoResponseDto> searchByEmail(String email);

    int register(Member member);

    int softDeleteIfActive(Long memberId, LocalDateTime now);
    void changeStatusLocked(String email);
    void changeStatusNormal(String email);

    int updateRepresentativeTitle(Long memberId, Long titleId);
}
