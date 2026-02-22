package com.ssafy.questory.security.config;

import com.ssafy.questory.member.domain.MemberStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Component;

@Component
public class MemberAuthPolicy {

    public void validateActive(MemberStatus status) {
        if (status == MemberStatus.SOFT_DELETE) {
            throw new DisabledException("Member is deleted");
        }
        if (status == MemberStatus.LOCKED) {
            throw new LockedException("Member is locked");
        }
    }
}
