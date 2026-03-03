package com.ssafy.questory.party.service;

import com.ssafy.questory.party.domain.Party;
import com.ssafy.questory.party.domain.PartyMember;
import com.ssafy.questory.party.domain.PartyMemberRole;
import com.ssafy.questory.party.dto.request.CreateAndUpdateDto;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import com.ssafy.questory.party.repository.PartyRepository;
import com.ssafy.questory.member.domain.SecurityMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final PartyMemberRepository partyMemberRepository;

    @Transactional
    public PartyInfoDto create(SecurityMember member, CreateAndUpdateDto dto) {
        Long creatorId = member.getMemberId();
        String partyName = dto.name();
        LocalDateTime now = LocalDateTime.now();

        Party party = Party.builder()
                .name(partyName)
                .creatorId(creatorId)
                .createdAt(now)
                .build();
        partyRepository.insert(party);

        PartyMember partyMember = PartyMember.builder()
                .partyId(party.getPartyId())
                .memberId(member.getMemberId())
                .role(PartyMemberRole.OWNER)
                .joinedAt(now)
                .build();
        partyMemberRepository.insert(partyMember);

        return PartyInfoDto.builder()
                .partyId(party.getPartyId())
                .name(partyName)
                .creatorId(creatorId)
                .createdAt(now)
                .build();
    }
}
