package com.ssafy.questory.party.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.party.domain.Party;
import com.ssafy.questory.party.domain.PartyMember;
import com.ssafy.questory.party.domain.PartyMemberRole;
import com.ssafy.questory.party.dto.request.CreateAndUpdateDto;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import com.ssafy.questory.party.repository.PartyRepository;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
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

        validateName(partyName);

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

    @Transactional
    public void updateName(SecurityMember member, Long partyId, @Valid CreateAndUpdateDto dto) {
        Long memberId = member.getMemberId();
        String partyName = dto.name();

        validateName(partyName);

        int updated = partyRepository.updateNameIfCreator(memberId, partyId, partyName);
        if (updated == 1) return;

        Party party = partyRepository.findByPartyId(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));
        if (!party.getCreatorId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_PARTY_CREATOR_ONLY);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new CustomException(ErrorCode.INVALID_PARTY_NAME);
        }
    }
}
