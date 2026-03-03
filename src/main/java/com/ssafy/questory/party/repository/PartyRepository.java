package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.Party;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PartyRepository {
    void insert(Party party);
}
