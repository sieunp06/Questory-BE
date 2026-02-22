package com.ssafy.questory.auth.service;

import com.ssafy.questory.auth.domain.AuthProvider;
import com.ssafy.questory.auth.domain.LoginPrincipal;
import com.ssafy.questory.auth.domain.MemberOAuthAccount;
import com.ssafy.questory.auth.oauth2.OAuth2UserInfo;
import com.ssafy.questory.auth.repository.MemberOAuthAccountRepository;
import com.ssafy.questory.auth.util.OAuth2UserInfoFactory;
import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.Member;
import com.ssafy.questory.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository oAuthAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        AuthProvider provider = AuthProvider.from(registrationId);

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.of(registrationId, oAuth2User.getAttributes());

        String providerMemberId = userInfo.getProviderUserId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.SOCIAL_EMAIL_REQUIRED);
        }

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
                                .nickname(nickname)
                                .build();
                        memberRepository.register(newMember);
                        return newMember;
                    });
            oAuthAccountRepository.insert(
                    MemberOAuthAccount.builder()
                            .memberId(member.getMemberId())
                            .provider(provider)
                            .providerMemberId(providerMemberId)
                            .build());
        }
        return new LoginPrincipal(member.getMemberId(), member.getEmail());
    }
}
