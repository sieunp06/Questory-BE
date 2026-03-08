package com.ssafy.questory.party.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.dto.request.InviteRequestDto;
import com.ssafy.questory.party.dto.response.InviteResponsesDto;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PartyInviteService {
    private final PartyMemberRepository partyMemberRepository;
    private final PartyInviteChunkService partyInviteChunkService;

    private static final int CHUNK_SIZE = 20;

    @Transactional
    public InviteResponsesDto invite(SecurityMember member, Long partyId, @Valid InviteRequestDto dto) {
        Long inviterId = member.getMemberId();
        List<Long> inviteesId = dto.inviteeIds();

        if (!partyMemberRepository.exists(partyId, inviterId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }

        Set<Long> deduplicated = new HashSet<>();
        for (Long id : inviteesId) {
            if (id == null) continue;
            if (inviterId.equals(id)) continue;
            deduplicated.add(id);
        }

        List<Long> targets = new ArrayList<>(deduplicated);
        Collections.sort(targets);

        List<InviteResponsesDto.InviteResultDto> results = new ArrayList<>();

        for (int i = 0; i < targets.size(); i += CHUNK_SIZE) {
            List<Long> chunk = targets.subList(i, Math.min(i + CHUNK_SIZE, targets.size()));
            results.addAll(
                    partyInviteChunkService.processChunkWithRetry(inviterId, partyId, new ArrayList<>(chunk))
            );
        }

        return InviteResponsesDto.builder()
                .results(results)
                .build();
    }
}
