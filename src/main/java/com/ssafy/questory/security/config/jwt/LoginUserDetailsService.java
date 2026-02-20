package com.ssafy.questory.security.config.jwt;

import com.ssafy.questory.security.config.MemberAuthPolicy;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import com.ssafy.questory.member.repository.MemberRepository;
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
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        LoginPrincipalRow row = memberRepository.findLoginPrincipalByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        memberAuthPolicy.validateActive(row.status());

        return SecurityMember.fromLogin(row);
    }

}
