package com.ssafy.questory.config.security;

import com.ssafy.questory.member.domain.MemberStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

@Component
public class MemberAuthPolicy {

    public void validateActive(String status) {
        MemberStatus s = MemberStatus.valueOf(status);
        if (s == MemberStatus.SOFT_DELETE) {
            throw new DisabledException("Member is deleted");
        }
        if (s == MemberStatus.LOCKED) {
            throw new LockedException("Member is locked");
        }
    }
}
