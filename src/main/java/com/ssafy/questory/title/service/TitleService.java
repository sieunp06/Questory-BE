package com.ssafy.questory.title.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.title.dto.request.AcquireTitleRequestDto;
import com.ssafy.questory.title.dto.request.UpdateRepresentativeTitleRequestDto;
import com.ssafy.questory.title.dto.response.TitleResponseDto;
import com.ssafy.questory.title.repository.MemberTitleRepository;
import com.ssafy.questory.title.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TitleService {
    private final MemberRepository memberRepository;
    private final TitleRepository titleRepository;
    private final MemberTitleRepository memberTitleRepository;

    @Transactional(readOnly = true)
    public List<TitleResponseDto> getMyTitles(SecurityMember member) {
        Long memberId = member.getMemberId();

        MemberStatus status = memberRepository.findStatusById(memberId);
        if (status == null) throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        if (status == MemberStatus.SOFT_DELETE) throw new CustomException(ErrorCode.MEMBER_DELETED);
        if (status == MemberStatus.LOCKED) throw new CustomException(ErrorCode.MEMBER_LOCKED);

        return memberTitleRepository.findOwnedTitles(memberId);
    }

    @Transactional
    public void acquireTitle(SecurityMember member, AcquireTitleRequestDto dto) {
        Long memberId = member.getMemberId();

        MemberStatus status = memberRepository.findStatusById(memberId);
        if (status == null) throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        if (status == MemberStatus.SOFT_DELETE) throw new CustomException(ErrorCode.MEMBER_DELETED);
        if (status == MemberStatus.LOCKED) throw new CustomException(ErrorCode.MEMBER_LOCKED);

        String titleName = titleRepository.findNameById(dto.titleId());
        if (titleName == null) {
            throw new CustomException(ErrorCode.TITLE_NOT_FOUND);
        }

        boolean owned = memberTitleRepository.existsByMemberIdAndTitleId(memberId, dto.titleId());
        if (owned) {
            throw new CustomException(ErrorCode.MEMBER_TITLE_ALREADY_OWNED);
        }

        int inserted = memberTitleRepository.insertMemberTitle(memberId, dto.titleId());
        if (inserted == 0) {
            throw new CustomException(ErrorCode.MEMBER_TITLE_ALREADY_OWNED);
        }
    }

    @Transactional
    public void updateRepresentativeTitle(SecurityMember member, UpdateRepresentativeTitleRequestDto dto) {
        Long memberId = member.getMemberId();

        MemberStatus status = memberRepository.findStatusById(memberId);
        if (status == null) throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        if (status == MemberStatus.SOFT_DELETE) throw new CustomException(ErrorCode.MEMBER_DELETED);
        if (status == MemberStatus.LOCKED) throw new CustomException(ErrorCode.MEMBER_LOCKED);

        boolean owned = memberTitleRepository.existsByMemberIdAndTitleId(memberId, dto.titleId());
        if (!owned) {
            throw new CustomException(ErrorCode.MEMBER_TITLE_NOT_OWNED);
        }

        int updated = memberRepository.updateRepresentativeTitle(memberId, dto.titleId());
        if (updated == 0) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
