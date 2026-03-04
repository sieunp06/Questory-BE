package com.ssafy.questory.party.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PartyInvite {
    private Long inviteId;
    private Long partyId;
    private Long inviterId;
    private Long inviteeId;
    private PartyInviteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
