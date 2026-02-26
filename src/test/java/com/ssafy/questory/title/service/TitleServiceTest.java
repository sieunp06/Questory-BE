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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock TitleRepository titleRepository;
    @Mock MemberTitleRepository memberTitleRepository;

    @InjectMocks TitleService titleService;

    private SecurityMember mockMember(Long memberId) {
        SecurityMember sm = mock(SecurityMember.class);
        when(sm.getMemberId()).thenReturn(memberId);
        return sm;
    }

    @Test
    @DisplayName("getMyTitles: 정상 회원이면 보유 칭호 목록을 조회한다")
    void getMyTitles_success() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);

        List<TitleResponseDto> expected = List.of(mock(TitleResponseDto.class));
        when(memberTitleRepository.findOwnedTitles(memberId)).thenReturn(expected);

        List<TitleResponseDto> result = titleService.getMyTitles(member);

        assertThat(result).isSameAs(expected);
        verify(memberRepository).findStatusById(memberId);
        verify(memberTitleRepository).findOwnedTitles(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("getMyTitles: status=null이면 MEMBER_NOT_FOUND")
    void getMyTitles_statusNull_memberNotFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findStatusById(memberId)).thenReturn(null);

        CustomException ex = catchThrowableOfType(
                () -> titleService.getMyTitles(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("getMyTitles: status=SOFT_DELETE이면 MEMBER_DELETED")
    void getMyTitles_deleted_memberDeleted() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.SOFT_DELETE);

        CustomException ex = catchThrowableOfType(
                () -> titleService.getMyTitles(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("getMyTitles: status=LOCKED이면 MEMBER_LOCKED")
    void getMyTitles_locked_memberLocked() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.LOCKED);

        CustomException ex = catchThrowableOfType(
                () -> titleService.getMyTitles(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_LOCKED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: 정상 흐름이면 (미보유 + insert 성공) 칭호를 획득한다")
    void acquireTitle_success() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(titleRepository.findNameById(titleId)).thenReturn("칭호");
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(false);
        when(memberTitleRepository.insertMemberTitle(memberId, titleId)).thenReturn(1);

        titleService.acquireTitle(member, dto);

        verify(memberRepository).findStatusById(memberId);
        verify(titleRepository).findNameById(titleId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verify(memberTitleRepository).insertMemberTitle(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: status=null이면 MEMBER_NOT_FOUND")
    void acquireTitle_statusNull_memberNotFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(null);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: status=SOFT_DELETE이면 MEMBER_DELETED")
    void acquireTitle_deleted_memberDeleted() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.SOFT_DELETE);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: status=LOCKED이면 MEMBER_LOCKED")
    void acquireTitle_locked_memberLocked() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.LOCKED);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_LOCKED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: title이 없으면 TITLE_NOT_FOUND")
    void acquireTitle_titleNotFound() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(titleRepository.findNameById(titleId)).thenReturn(null);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.TITLE_NOT_FOUND);

        verify(memberRepository).findStatusById(memberId);
        verify(titleRepository).findNameById(titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: 이미 보유한 칭호면 MEMBER_TITLE_ALREADY_OWNED")
    void acquireTitle_alreadyOwned_existsTrue() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(titleRepository.findNameById(titleId)).thenReturn("칭호");
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(true);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_TITLE_ALREADY_OWNED);

        verify(memberRepository).findStatusById(memberId);
        verify(titleRepository).findNameById(titleId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("acquireTitle: insert 결과가 0이면(동시성 등) MEMBER_TITLE_ALREADY_OWNED")
    void acquireTitle_insert0_treatedAsAlreadyOwned() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        AcquireTitleRequestDto dto = mock(AcquireTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(titleRepository.findNameById(titleId)).thenReturn("칭호");
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(false);
        when(memberTitleRepository.insertMemberTitle(memberId, titleId)).thenReturn(0);

        CustomException ex = catchThrowableOfType(
                () -> titleService.acquireTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_TITLE_ALREADY_OWNED);

        verify(memberRepository).findStatusById(memberId);
        verify(titleRepository).findNameById(titleId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verify(memberTitleRepository).insertMemberTitle(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: 보유한 칭호이고 update 성공(1)이면 대표 칭호가 변경된다")
    void updateRepresentativeTitle_success() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(true);
        when(memberRepository.updateRepresentativeTitle(memberId, titleId)).thenReturn(1);

        titleService.updateRepresentativeTitle(member, dto);

        verify(memberRepository).findStatusById(memberId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verify(memberRepository).updateRepresentativeTitle(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: status=null이면 MEMBER_NOT_FOUND")
    void updateRepresentativeTitle_statusNull_memberNotFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(null);

        CustomException ex = catchThrowableOfType(
                () -> titleService.updateRepresentativeTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: status=SOFT_DELETE이면 MEMBER_DELETED")
    void updateRepresentativeTitle_deleted_memberDeleted() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.SOFT_DELETE);

        CustomException ex = catchThrowableOfType(
                () -> titleService.updateRepresentativeTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: status=LOCKED이면 MEMBER_LOCKED")
    void updateRepresentativeTitle_locked_memberLocked() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.LOCKED);

        CustomException ex = catchThrowableOfType(
                () -> titleService.updateRepresentativeTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_LOCKED);

        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: 보유하지 않은 칭호면 MEMBER_TITLE_NOT_OWNED")
    void updateRepresentativeTitle_notOwned_memberTitleNotOwned() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(false);

        CustomException ex = catchThrowableOfType(
                () -> titleService.updateRepresentativeTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_TITLE_NOT_OWNED);

        verify(memberRepository).findStatusById(memberId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }

    @Test
    @DisplayName("updateRepresentativeTitle: update 결과가 0이면 MEMBER_NOT_FOUND")
    void updateRepresentativeTitle_update0_memberNotFound() {
        Long memberId = 1L;
        long titleId = 10L;

        SecurityMember member = mockMember(memberId);
        UpdateRepresentativeTitleRequestDto dto = mock(UpdateRepresentativeTitleRequestDto.class);
        when(dto.titleId()).thenReturn(titleId);

        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);
        when(memberTitleRepository.existsByMemberIdAndTitleId(memberId, titleId)).thenReturn(true);
        when(memberRepository.updateRepresentativeTitle(memberId, titleId)).thenReturn(0);

        CustomException ex = catchThrowableOfType(
                () -> titleService.updateRepresentativeTitle(member, dto),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findStatusById(memberId);
        verify(memberTitleRepository).existsByMemberIdAndTitleId(memberId, titleId);
        verify(memberRepository).updateRepresentativeTitle(memberId, titleId);
        verifyNoMoreInteractions(memberRepository, titleRepository, memberTitleRepository);
    }
}