package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.dto.request.LoginRequestDto;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.dto.response.TokenResponseDto;
import com.ssafy.questory.member.repository.MemberPasswordCredentialsRepository;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.security.config.jwt.JwtService;
import com.ssafy.questory.security.config.jwt.LoginUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @Mock MemberPasswordCredentialsRepository memberPasswordRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Mock AuthenticationManager authenticationManager;
    @Mock LoginUserDetailsService userDetailsService;
    @Mock JwtService jwtService;

    @InjectMocks MemberService memberService;

    @Test
    @DisplayName("회원가입 성공: member 저장 + password 저장 + 응답 반환")
    void register_success() throws Exception {
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
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));

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
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_DELETED));

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
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.PASSWORD_CONFIRM_MISMATCH));

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
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS));

        then(memberRepository).should(never()).register(any());
        then(memberPasswordRepository).shouldHaveNoInteractions();
        then(passwordEncoder).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 성공: authenticate + 토큰 발급 + TokenResponseDto 반환")
    void login_success() {
        LoginRequestDto req = new LoginRequestDto("test@example.com", "Aa1!aaaa");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.NORMAL);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        willReturn(null).given(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetailsService.loadUserByUsername("test@example.com")).willReturn(userDetails);

        given(jwtService.generateAccessToken(userDetails)).willReturn("ACCESS");
        given(jwtService.generateRefreshToken(userDetails)).willReturn("REFRESH");

        TokenResponseDto res = memberService.login(req);

        assertThat(res.email()).isEqualTo("test@example.com");
        assertThat(res.accessToken()).isEqualTo("ACCESS");
        assertThat(res.refreshToken()).isEqualTo("REFRESH");

        then(authenticationManager).should(times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        then(userDetailsService).should(times(1)).loadUserByUsername("test@example.com");
        then(jwtService).should(times(1)).generateAccessToken(userDetails);
        then(jwtService).should(times(1)).generateRefreshToken(userDetails);
    }

    @Test
    @DisplayName("로그인 실패: 회원 없음 -> MEMBER_NOT_FOUND (authenticate 호출 안 함)")
    void login_fail_memberNotFound() {
        LoginRequestDto req = new LoginRequestDto("nope@example.com", "Aa1!aaaa");

        given(memberRepository.findByEmail("nope@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

        then(authenticationManager).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 실패: 탈퇴 회원 -> MEMBER_DELETED (authenticate 호출 안 함)")
    void login_fail_deletedMember() {
        LoginRequestDto req = new LoginRequestDto("test@example.com", "Aa1!aaaa");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.SOFT_DELETE);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_DELETED));

        then(authenticationManager).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 실패: 잠김 회원 -> MEMBER_LOCKED (authenticate 호출 안 함)")
    void login_fail_lockedMember() {
        LoginRequestDto req = new LoginRequestDto("test@example.com", "Aa1!aaaa");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.LOCKED);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_LOCKED));

        then(authenticationManager).shouldHaveNoInteractions();
        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호 불일치 -> INVALID_PASSWORD")
    void login_fail_invalidPassword() {
        LoginRequestDto req = new LoginRequestDto("test@example.com", "wrong");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.NORMAL);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        willThrow(new BadCredentialsException("bad"))
                .given(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));

        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로그인 실패: DisabledException -> MEMBER_NOT_FOUND (서비스 매핑 로직 검증)")
    void login_fail_disabledException_mappedToMemberNotFound() {
        LoginRequestDto req = new LoginRequestDto("test@example.com", "Aa1!aaaa");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.NORMAL);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        willThrow(new DisabledException("disabled"))
                .given(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("refresh 성공: refreshToken에서 email 추출 + accessToken 재발급")
    void refresh_success() {
        given(jwtService.extractUsername("REFRESH_TOKEN", JwtService.TokenType.REFRESH))
                .willReturn("test@example.com");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.NORMAL);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetailsService.loadUserByUsername("test@example.com")).willReturn(userDetails);

        given(jwtService.generateAccessToken(userDetails)).willReturn("NEW_ACCESS");

        String newAccess = memberService.refresh("REFRESH_TOKEN");

        assertThat(newAccess).isEqualTo("NEW_ACCESS");

        then(jwtService).should(times(1))
                .extractUsername("REFRESH_TOKEN", JwtService.TokenType.REFRESH);
        then(userDetailsService).should(times(1)).loadUserByUsername("test@example.com");
        then(jwtService).should(times(1)).generateAccessToken(userDetails);
    }

    @Test
    @DisplayName("refresh 실패: 회원 없음 -> MEMBER_NOT_FOUND")
    void refresh_fail_memberNotFound() {
        given(jwtService.extractUsername("REFRESH_TOKEN", JwtService.TokenType.REFRESH))
                .willReturn("nope@example.com");
        given(memberRepository.findByEmail("nope@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.refresh("REFRESH_TOKEN"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));

        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).should(never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("refresh 실패: 탈퇴 회원 -> MEMBER_DELETED")
    void refresh_fail_deleted() {
        given(jwtService.extractUsername("REFRESH_TOKEN", JwtService.TokenType.REFRESH))
                .willReturn("test@example.com");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.SOFT_DELETE);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.refresh("REFRESH_TOKEN"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_DELETED));

        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).should(never()).generateAccessToken(any());
    }

    @Test
    @DisplayName("refresh 실패: 잠김 회원 -> MEMBER_LOCKED")
    void refresh_fail_locked() {
        given(jwtService.extractUsername("REFRESH_TOKEN", JwtService.TokenType.REFRESH))
                .willReturn("test@example.com");

        Member member = mock(Member.class);
        given(member.getStatus()).willReturn(MemberStatus.LOCKED);
        given(memberRepository.findByEmail("test@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.refresh("REFRESH_TOKEN"))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_LOCKED));

        then(userDetailsService).shouldHaveNoInteractions();
        then(jwtService).should(never()).generateAccessToken(any());
    }
}