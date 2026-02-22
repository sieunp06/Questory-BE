package com.ssafy.questory.member.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Member {
    private Long memberId;
    private String email;
    private String nickname;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    protected Member() {}

    @Builder
    private Member(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
        this.status = MemberStatus.NORMAL;
    }
}
