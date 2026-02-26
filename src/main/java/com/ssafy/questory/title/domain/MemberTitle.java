package com.ssafy.questory.title.domain;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberTitle {
    private Long memberId;
    private Long titleId;
    private LocalDateTime acquiredAt;

    protected MemberTitle() {}

    @Builder
    private MemberTitle(Long memberId, Long titleId, LocalDateTime acquiredAt) {
        this.memberId = memberId;
        this.titleId = titleId;
        this.acquiredAt = acquiredAt;
    }
}