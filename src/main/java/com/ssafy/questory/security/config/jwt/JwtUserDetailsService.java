package com.ssafy.questory.security.config.jwt;

import com.ssafy.questory.security.config.MemberAuthPolicy;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberAuthPolicy memberAuthPolicy;

    public UserDetails loadUserByEmail(String email) {
        LoginPrincipalRow row = memberRepository.findLoginPrincipalByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        memberAuthPolicy.validateActive(row.status());

        return SecurityMember.fromLogin(row);
    }
}
