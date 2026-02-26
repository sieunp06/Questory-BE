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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberPasswordCredentialsRepository memberPasswordRepository;
    private final LoginFailureService loginFailureService;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final LoginUserDetailsService userDetailsService;
    private final JwtService jwtService;

    private static final int MAX_FAILED_LOGIN_COUNT = 5;

    @Transactional
    public MemberResponseDto register(RegisterRequestDto dto) {
        String email = dto.email();
        String password = dto.password();
        String passwordConfirm = dto.passwordConfirm();
        String nickname = dto.nickname();

        validateNotExistMember(email);
        validatePassword(password, passwordConfirm);

        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .build();

        memberRepository.register(member);
        memberPasswordRepository.register(member.getMemberId(), passwordEncoder.encode(password));

        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }

    public TokenResponseDto login(LoginRequestDto dto) {
        String email = dto.email();
        String password = dto.password();
        LocalDateTime now = LocalDateTime.now();

        validateExistAndActiveMemberWithAutoUnlock(email);

        authenticate(email, password, now);

        memberPasswordRepository.resetFailedLoginCount(email);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return TokenResponseDto.builder()
                .email(email)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String refresh(String refreshToken) {
        String email = jwtService.extractUsername(refreshToken, JwtService.TokenType.REFRESH);

        validateExistAndActiveMemberWithAutoUnlock(email);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return jwtService.generateAccessToken(userDetails);
    }

    private void authenticate(String email, String password, LocalDateTime now) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        } catch (BadCredentialsException e) {
            int failedCount = loginFailureService.updateFailedCount(email, now);

            if (failedCount >= MAX_FAILED_LOGIN_COUNT) {
                loginFailureService.lockAccount(email);
                throw new CustomException(ErrorCode.MEMBER_LOCKED);
            }
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private void validateNotExistMember(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> {
            if (member.getStatus().equals(MemberStatus.SOFT_DELETE)) {
                throw new CustomException(ErrorCode.MEMBER_DELETED);
            }
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
    }

    private void validateExistAndActiveMemberWithAutoUnlock(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus().equals(MemberStatus.SOFT_DELETE)) {
            throw new CustomException(ErrorCode.MEMBER_DELETED);
        }
        if (member.getStatus().equals(MemberStatus.LOCKED)) {
            LocalDateTime lockedUntil = memberPasswordRepository.findLockedUntilByEmail(email)
                    .orElse(LocalDateTime.MIN);

            if (lockedUntil.isAfter(LocalDateTime.now())) {
                throw new CustomException(ErrorCode.MEMBER_LOCKED);
            }

            loginFailureService.unlockAccount(email);
        }
    }

    private void validatePassword(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }
}
