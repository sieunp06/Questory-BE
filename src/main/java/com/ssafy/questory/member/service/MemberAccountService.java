package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.response.MemberInfoResponseDto;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAccountService {
    private final MemberRepository memberRepository;

    public MemberInfoResponseDto getUserInfo(SecurityMember member) {
        Long memberId = member.getMemberId();
        return memberRepository.findMyInfo(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void withdraw(SecurityMember member) {
        Long memberId = member.getMemberId();
        LocalDateTime now = LocalDateTime.now();

        int result = memberRepository.softDeleteIfActive(memberId, now);
        if (result == 0) {
            validateStatus(memberId);
        }
    }

    public List<MemberInfoResponseDto> search(SecurityMember member, String email) {
        Long memberId = member.getMemberId();

        validateExistsMember(memberId);

        if (email == null) {
            return List.of();
        }
        String keyword = email.trim();
        if (keyword.isEmpty()) {
            return List.of();
        }

        return memberRepository.searchByEmail(keyword);
    }

    private void validateExistsMember(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
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
