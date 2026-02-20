package com.ssafy.questory.member.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.dto.request.RegisterRequestDto;
import com.ssafy.questory.member.dto.response.MemberResponseDto;
import com.ssafy.questory.member.repository.MemberPasswordCredentialsRepository;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberPasswordCredentialsRepository memberPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponseDto register(RegisterRequestDto memberRegistRequestDto) {
        String email = memberRegistRequestDto.email();
        String password = memberRegistRequestDto.password();
        String passwordConfirm = memberRegistRequestDto.passwordConfirm();
        String nickname = memberRegistRequestDto.nickname();

        validateExistMember(email);
        validatePassword(password, passwordConfirm);
        validateNickname(nickname);

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

    private void validateExistMember(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> {
            if (member.getStatus().equals(MemberStatus.SOFT_DELETE)) {
                throw new CustomException(ErrorCode.MEMBER_DELETED);
            }
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
    }

    private void validatePassword(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new CustomException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }

    private void validateNickname(String nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }
}
