package com.ssafy.questory.group.repository;

import com.ssafy.questory.group.domain.PartyMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartyMemberRepository {
    void insert(PartyMember partyMember);
}
