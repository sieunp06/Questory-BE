package com.ssafy.questory.party.repository;

import com.ssafy.questory.party.domain.PartyMember;
import com.ssafy.questory.party.dto.response.PartyMemberInfoDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PartyMemberRepository {
    void insert(PartyMember partyMember);

    List<PartyMemberInfoDto> findMembersByPartyId(Long partyId);
    List<Long> findExistingMemberIds(Long partyId, List<Long> targets);

    boolean exists(Long partyId, Long memberId);

    boolean isOwner(Long partyId, Long memberId);
    int demoteOwnerToMember(Long partyId, Long memberId);

    int promoteMemberToOwner(Long partyId, Long memberId);

    int delete(Long partyId, Long memberId);
}
