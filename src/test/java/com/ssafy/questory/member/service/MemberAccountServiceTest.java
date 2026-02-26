package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.response.MemberInfoResponseDto;
import com.ssafy.questory.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberAccountServiceTest {
    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberAccountService memberAccountService;

    private SecurityMember mockMember(Long memberId) {
        SecurityMember sm = mock(SecurityMember.class);
        when(sm.getMemberId()).thenReturn(memberId);
        return sm;
    }

    @Test
    @DisplayName("getUserInfo: 내 정보가 존재하면 반환한다")
    void getUserInfo_success() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        MemberInfoResponseDto dto = mock(MemberInfoResponseDto.class);
        when(memberRepository.findMyInfo(memberId)).thenReturn(Optional.of(dto));

        MemberInfoResponseDto result = memberAccountService.getUserInfo(member);

        assertThat(result).isSameAs(dto);
        verify(memberRepository).findMyInfo(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("getUserInfo: 내 정보가 없으면 MEMBER_NOT_FOUND 예외")
    void getUserInfo_notFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findMyInfo(memberId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.getUserInfo(member),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        verify(memberRepository).findMyInfo(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("withdraw: ACTIVE(정상) 회원이면 soft delete 업데이트가 1건 발생하고 종료한다")
    void withdraw_success_softDeleteUpdated() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.softDeleteIfActive(eq(memberId), any(LocalDateTime.class)))
                .thenReturn(1);

        memberAccountService.withdraw(member);

        verify(memberRepository).softDeleteIfActive(eq(memberId), any(LocalDateTime.class));
        verify(memberRepository, never()).findStatusById(anyLong());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("withdraw: softDeleteIfActive 결과가 0이고 status가 null이면 MEMBER_NOT_FOUND")
    void withdraw_result0_statusNull_memberNotFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.softDeleteIfActive(eq(memberId), any(LocalDateTime.class)))
                .thenReturn(0);
        when(memberRepository.findStatusById(memberId)).thenReturn(null);

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.withdraw(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).softDeleteIfActive(eq(memberId), any(LocalDateTime.class));
        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("withdraw: softDeleteIfActive 결과가 0이고 status가 SOFT_DELETE면 MEMBER_DELETED")
    void withdraw_result0_statusSoftDelete_memberDeleted() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.softDeleteIfActive(eq(memberId), any(LocalDateTime.class)))
                .thenReturn(0);
        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.SOFT_DELETE);

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.withdraw(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);

        verify(memberRepository).softDeleteIfActive(eq(memberId), any(LocalDateTime.class));
        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("withdraw: softDeleteIfActive 결과가 0이고 status가 LOCKED면 MEMBER_LOCKED")
    void withdraw_result0_statusLocked_memberLocked() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.softDeleteIfActive(eq(memberId), any(LocalDateTime.class)))
                .thenReturn(0);
        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.LOCKED);

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.withdraw(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_LOCKED);

        verify(memberRepository).softDeleteIfActive(eq(memberId), any(LocalDateTime.class));
        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("withdraw: softDeleteIfActive 결과가 0이고 status가 그 외면 INVALID_MEMBER_STATUS")
    void withdraw_result0_statusOther_invalidStatus() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.softDeleteIfActive(eq(memberId), any(LocalDateTime.class)))
                .thenReturn(0);
        when(memberRepository.findStatusById(memberId)).thenReturn(MemberStatus.NORMAL);

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.withdraw(member),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_MEMBER_STATUS);

        verify(memberRepository).softDeleteIfActive(eq(memberId), any(LocalDateTime.class));
        verify(memberRepository).findStatusById(memberId);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("search: 요청자(memberId)가 존재하지 않으면 MEMBER_NOT_FOUND")
    void search_requesterNotFound() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(
                () -> memberAccountService.search(member, "test@example.com"),
                CustomException.class
        );
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(memberRepository).findById(memberId);
        verify(memberRepository, never()).searchByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("search: email이 null이면 빈 리스트 반환 + searchByEmail 호출 안함")
    void search_emailNull_returnsEmpty() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mock(Member.class)));

        List<MemberInfoResponseDto> result = memberAccountService.search(member, null);

        assertThat(result).isEmpty();
        verify(memberRepository).findById(memberId);
        verify(memberRepository, never()).searchByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("search: email이 공백만이면 빈 리스트 반환 + searchByEmail 호출 안함")
    void search_emailBlank_returnsEmpty() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mock(Member.class)));

        List<MemberInfoResponseDto> result = memberAccountService.search(member, "   ");

        assertThat(result).isEmpty();
        verify(memberRepository).findById(memberId);
        verify(memberRepository, never()).searchByEmail(anyString());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    @DisplayName("search: email이 있으면 trim 후 keyword로 searchByEmail 호출")
    void search_success_trimAndSearch() {
        Long memberId = 1L;
        SecurityMember member = mockMember(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mock(Member.class)));

        List<MemberInfoResponseDto> expected = List.of(mock(MemberInfoResponseDto.class));
        when(memberRepository.searchByEmail("test@example.com")).thenReturn(expected);

        List<MemberInfoResponseDto> result = memberAccountService.search(member, "  test@example.com  ");

        assertThat(result).isSameAs(expected);
        verify(memberRepository).findById(memberId);
        verify(memberRepository).searchByEmail("test@example.com");
        verifyNoMoreInteractions(memberRepository);
    }
}