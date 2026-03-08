package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyInvite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PartyInviteRepository {
    int insert(PartyInvite partyInvite);
    void bulkInsert(List<PartyInvite> invites);
}
