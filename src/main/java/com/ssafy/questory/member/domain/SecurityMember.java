package com.ssafy.questory.member.domain;

import com.ssafy.questory.member.dto.security.LoginPrincipalRow;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


public class SecurityMember implements UserDetails {
    private final Long memberId;
    private final String email;
    private final String nickname;
    private final String passwordHash;
    private final MemberStatus status;
    private final Integer failedLoginCount;
    private final LocalDateTime lockedUntil;

    private SecurityMember(
            Long memberId,
            String email,
            String nickname,
            String passwordHash,
            MemberStatus status,
            Integer failedLoginCount,
            LocalDateTime lockedUntil
    ) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
        this.status = status;
        this.failedLoginCount = failedLoginCount;
        this.lockedUntil = lockedUntil;
    }

    public static SecurityMember fromLogin(LoginPrincipalRow row) {
        return new SecurityMember(
                row.memberId(),
                row.email(),
                row.nickname(),
                row.passwordHash(),
                MemberStatus.valueOf(row.status()),
                row.failedLoginCount(),
                row.lockedUntil()
        );
    }

    public static SecurityMember fromMember(Member member) {
        return new SecurityMember(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                "",
                member.getStatus(),
                0,
                null
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return !"SOFT_DELETE".equals(status);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
