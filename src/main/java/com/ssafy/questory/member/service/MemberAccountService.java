package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberAccountService {
    private final MemberRepository memberRepository;

    @Transactional
    public void withdraw(SecurityMember member) {
        Long memberId = member.getMemberId();
        LocalDateTime now = LocalDateTime.now();

        int result = memberRepository.softDeleteIfActive(memberId, now);
        if (result == 0) {
            validateStatus(memberId);
        }
    }

    private void validateStatus(Long memberId) {
        MemberStatus status = memberRepository.findStatusById(memberId);

        if (status == null) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (status.equals(MemberStatus.SOFT_DELETE)) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }
        if (status.equals(MemberStatus.LOCKED)) {
            throw new CustomException(ErrorCode.MEMBER_LOCKED);
        }
        throw new CustomException(ErrorCode.INVALID_MEMBER_STATUS);
    }
}
