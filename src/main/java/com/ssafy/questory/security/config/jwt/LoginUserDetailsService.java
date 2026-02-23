package com.ssafy.questory.security.config.jwt;

import com.ssafy.questory.member.domain.MemberStatus;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.security.config.MemberAuthPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberAuthPolicy memberAuthPolicy;

    public LoginUserDetailsService(MemberRepository memberRepository, MemberAuthPolicy memberAuthPolicy) {
        this.memberRepository = memberRepository;
        this.memberAuthPolicy = memberAuthPolicy;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        LoginPrincipalRow row = memberRepository.findLoginPrincipalByEmailWithPassword(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        memberAuthPolicy.validateActive(MemberStatus.valueOf(row.status()));
        return SecurityMember.fromLogin(row);
    }
}