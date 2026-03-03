package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartyMemberRepository {
    void insert(PartyMember partyMember);
}
