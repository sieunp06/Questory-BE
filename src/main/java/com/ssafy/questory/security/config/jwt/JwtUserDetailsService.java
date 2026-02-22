package com.ssafy.questory.security.config.jwt;

import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.member.repository.MemberRepository;
import com.ssafy.questory.security.config.MemberAuthPolicy;
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
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        memberAuthPolicy.validateActive(member.getStatus());

        return SecurityMember.fromMember(member);
    }
}