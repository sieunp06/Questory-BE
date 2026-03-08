package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyInvite;
import com.ssafy.questory.party.domain.PartyInviteStatus;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PartyInviteRepository {
    int insert(PartyInvite partyInvite);
    void bulkInsert(List<PartyInvite> invites);

    int updateStatusByInviter(Long inviteId, Long inviterId, PartyInviteStatus status, LocalDateTime now);
}
