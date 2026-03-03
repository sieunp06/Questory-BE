package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.Party;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartyRepository {
    void insert(Party party);

    Optional<Party> findByPartyId(Long partyId);
    List<PartyInfoDto> findMyParties(Long memberId);

    int updateNameIfCreator(Long partyId, Long memberId, String name);

    int deleteIfCreator(Long memberId, Long partyId);
}
