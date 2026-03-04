package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyInvite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartyInviteRepository {
    int insert(PartyInvite partyInvite);
}
