package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.Party;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface PartyRepository {
    void insert(Party party);

    Optional<Party> findByPartyId(Long partyId);

    int updateNameIfCreator(Long partyId, Long memberId, String name);

    int deleteIfCreator(Long memberId, Long partyId);
}
