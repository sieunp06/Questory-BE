package com.ssafy.questory.party.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PartyMember {
    private Long partyId;
    private Long memberId;
    private PartyMemberRole role;
    private LocalDateTime joinedAt;
}
