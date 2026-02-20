package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.repository.MemberPasswordCredentialsRepository;
import com.ssafy.questory.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock MemberRepository memberRepository;
    @Mock MemberPasswordCredentialsRepository memberPasswordRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks MemberService memberService;

    @Test
    @DisplayName("회원가입 성공: member 저장 + password 저장 + 응답 반환")
    void register_success() {
        RegisterRequestDto req = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!aaaa",
                "테스터"
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(memberRepository.existsByNickname("테스터")).willReturn(false);
        given(passwordEncoder.encode("Aa1!aaaa")).willReturn("ENC");

        willAnswer(inv -> {
            Member m = inv.getArgument(0);
            var field = Member.class.getDeclaredField("memberId");
            field.setAccessible(true);
            field.set(m, 1L);
            return 1;
        }).given(memberRepository).register(any(Member.class));

        MemberResponseDto res = memberService.register(req);

        assertThat(res.memberId()).isEqualTo(1L);
        assertThat(res.email()).isEqualTo("test@example.com");
        assertThat(res.nickname()).isEqualTo("테스터");

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        then(memberRepository).should(times(1)).register(memberCaptor.capture());
        Member saved = memberCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getNickname()).isEqualTo("테스터");

        then(passwordEncoder).should(times(1)).encode("Aa1!aaaa");
        then(memberPasswordRepository).should(times(1)).register(1L, "ENC");
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일(NORMAL) -> EMAIL_ALREADY_EXISTS")
    void register_fail_emailAlreadyExists() {
        RegisterRequestDto req = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!aaaa",
                "테스터"
        );

        Member existing = mock(Member.class);
        given(existing.getStatus()).willReturn(MemberStatus.NORMAL);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
                });

        then(memberRepository).should(never()).register(any());
        then(memberPasswordRepository).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원가입 실패: 탈퇴 회원 이메일(SOFT_DELETE) -> MEMBER_DELETED")
    void register_fail_deletedMemberEmail() {
        RegisterRequestDto req = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!aaaa",
                "테스터"
        );

        Member existing = mock(Member.class);
        given(existing.getStatus()).willReturn(MemberStatus.SOFT_DELETE);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.MEMBER_DELETED);
                });

        then(memberRepository).should(never()).register(any());
        then(memberPasswordRepository).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원가입 실패: 비밀번호 확인 불일치 -> PASSWORD_CONFIRM_MISMATCH (repo 호출 없음)")
    void register_fail_passwordConfirmMismatch() {
        RegisterRequestDto req = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!bbbb",
                "테스터"
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
                });

        then(memberRepository).should(never()).existsByNickname(anyString());
        then(memberRepository).should(never()).register(any());
        then(memberPasswordRepository).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원가입 실패: 닉네임 중복 -> NICKNAME_ALREADY_EXISTS")
    void register_fail_nicknameAlreadyExists() {
        RegisterRequestDto req = new RegisterRequestDto(
                "test@example.com",
                "Aa1!aaaa",
                "Aa1!aaaa",
                "테스터"
        );

        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(memberRepository.existsByNickname("테스터")).willReturn(true);

        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS);
                });

        then(memberRepository).should(never()).register(any());
        then(memberPasswordRepository).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }
}
