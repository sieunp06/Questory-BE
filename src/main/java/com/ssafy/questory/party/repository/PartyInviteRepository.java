package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyInvite;
import com.ssafy.questory.party.domain.PartyInviteStatus;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface PartyInviteRepository {
    int insert(PartyInvite partyInvite);
    void bulkInsert(List<PartyInvite> invites);

    Optional<PartyInvite> findByInviteId(Long inviteId);

    int updateStatusByInviter(Long inviteId, Long inviterId, PartyInviteStatus status, LocalDateTime now);
    int updateStatusByInvitee(Long inviteId, Long inviteeId, PartyInviteStatus status, LocalDateTime now);
}
