package com.ssafy.questory.auth.repository;

import com.ssafy.questory.auth.domain.AuthProvider;
import com.ssafy.questory.auth.domain.MemberOAuthAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface MemberOAuthAccountRepository {
    Optional<MemberOAuthAccount> findByProviderAndProviderMemberId(AuthProvider provider, String providerMemberId);
    void insert(MemberOAuthAccount memberOAuthAccount);
}
