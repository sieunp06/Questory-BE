package com.ssafy.questory.party.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.domain.PartyInvite;
import com.ssafy.questory.party.domain.PartyInviteStatus;
import com.ssafy.questory.party.domain.PartyMember;
import com.ssafy.questory.party.domain.PartyMemberRole;
import com.ssafy.questory.party.dto.request.InviteRequestDto;
import com.ssafy.questory.party.dto.response.InviteResponsesDto;
import com.ssafy.questory.party.repository.PartyInviteRepository;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PartyInviteService {
    private final PartyMemberRepository partyMemberRepository;
    private final PartyInviteRepository partyInviteRepository;
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

    @Transactional
    public void cancel(SecurityMember member, Long inviteId) {
        Long inviterId = member.getMemberId();
        int updated = partyInviteRepository.updateStatusByInviter(
                inviteId, inviterId, PartyInviteStatus.CANCELED, LocalDateTime.now());

        if (updated == 0) {
            throw new CustomException(ErrorCode.PARTY_INVITE_CANCEL_NOT_ALLOWED);
        }
    }

    @Transactional
    public void reject(SecurityMember member, Long inviteId) {
        Long inviteeId = member.getMemberId();
        int updated = partyInviteRepository.updateStatusByInvitee(
                inviteId,
                inviteeId,
                PartyInviteStatus.REJECTED,
                LocalDateTime.now()
        );

        if (updated == 0) {
            throw new CustomException(ErrorCode.PARTY_INVITE_REJECT_NOT_ALLOWED);
        }
    }

    @Transactional
    public void accept(SecurityMember member, Long inviteId) {
        Long inviteeId = member.getMemberId();

        PartyInvite invite = partyInviteRepository.findByInviteId(inviteId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_INVITE_NOT_FOUND));

        if (!invite.getInviteeId().equals(inviteeId)) {
            throw new CustomException(ErrorCode.PARTY_INVITE_ACCEPT_NOT_ALLOWED);
        }

        if (invite.getStatus() != PartyInviteStatus.PENDING) {
            throw new CustomException(ErrorCode.PARTY_INVITE_ALREADY_PROCESSED);
        }

        if (partyMemberRepository.exists(invite.getPartyId(), inviteeId)) {
            throw new CustomException(ErrorCode.ALREADY_PARTY_MEMBER);
        }

        try {
            partyMemberRepository.insert(PartyMember.builder()
                    .partyId(invite.getPartyId())
                    .memberId(inviteeId)
                    .role(PartyMemberRole.MEMBER)
                    .joinedAt(LocalDateTime.now())
                    .build());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.ALREADY_PARTY_MEMBER);
        }

        int updated = partyInviteRepository.updateStatusByInvitee(
                inviteId,
                inviteeId,
                PartyInviteStatus.ACCEPTED,
                LocalDateTime.now()
        );

        if (updated == 0) {
            throw new CustomException(ErrorCode.PARTY_INVITE_ALREADY_PROCESSED);
        }
    }
}
