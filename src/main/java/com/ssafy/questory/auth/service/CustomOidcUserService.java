package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.domain.AuthProvider;
import com.ssafy.questory.auth.domain.MemberOAuthAccount;
import com.ssafy.questory.auth.repository.MemberOAuthAccountRepository;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository oAuthAccountRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.from(registrationId);

        String providerMemberId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String nickname = oidcUser.getFullName();

        Long memberId = linkOrCreate(provider, providerMemberId, email, nickname);

        var claims = new java.util.HashMap<>(oidcUser.getClaims());
        claims.put("memberId", memberId);

        Set<GrantedAuthority> authorities = Set.copyOf(oidcUser.getAuthorities());
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub") {
            @Override public java.util.Map<String, Object> getClaims() { return claims; }
        };
    }

    @Transactional
    protected Long linkOrCreate(AuthProvider provider, String providerMemberId, String email, String nickname) {
        Optional<MemberOAuthAccount> existingLink =
                oAuthAccountRepository.findByProviderAndProviderMemberId(provider, providerMemberId);

        Member member;
        if (existingLink.isPresent()) {
            member = memberRepository.findById(existingLink.get().getMemberId())
                    .orElseThrow(() -> new IllegalStateException("Linked member not found"));
        } else {
            member = memberRepository.findByEmail(email)
                    .orElseGet(() -> {
                        Member newMember = Member.builder()
                                .email(email)
                                .nickname(nickname != null ? nickname : provider.name() + "_" + providerMemberId.substring(0, 8))
                                .build();
                        memberRepository.register(newMember);
                        return newMember;
                    });

            oAuthAccountRepository.insert(
                    MemberOAuthAccount.builder()
                            .memberId(member.getMemberId())
                            .provider(provider)
                            .providerMemberId(providerMemberId)
                            .build()
            );
        }
        return member.getMemberId();
    }
}