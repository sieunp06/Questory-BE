package com.ssafy.questory.party.service;

import com.ssafy.questory.friend.repository.FriendRepository;
import com.ssafy.questory.party.domain.PartyInvite;
import com.ssafy.questory.party.domain.PartyInviteStatus;
import com.ssafy.questory.party.dto.response.InviteResponsesDto;
import com.ssafy.questory.party.repository.PartyInviteRepository;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PartyInviteChunkService {
    private static final int MAX_RETRY = 3;

    private final FriendRepository friendRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final PartyInviteRepository partyInviteRepository;

    public List<InviteResponsesDto.InviteResultDto> processChunkWithRetry(
            Long inviterId,
            Long partyId,
            List<Long> chunk
    ) {
        int attempt = 0;

        while (true) {
            try {
                return processChunk(inviterId, partyId, chunk);
            } catch (DeadlockLoserDataAccessException e) {
                attempt++;
                if (attempt >= MAX_RETRY) {
                    throw e;
                }

                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Retry interrupted", ie);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<InviteResponsesDto.InviteResultDto> processChunk(
            Long inviterId,
            Long partyId,
            List<Long> chunk
    ) {
        Set<Long> friendSet = new HashSet<>(
                friendRepository.findFriendIdsAmong(inviterId, chunk)
        );

        Set<Long> alreadyMemberSet = new HashSet<>(
                partyMemberRepository.findExistingMemberIds(partyId, chunk)
        );

        List<InviteResponsesDto.InviteResultDto> results = new ArrayList<>();
        List<PartyInvite> toCreate = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (Long inviteeId : chunk) {
            if (!friendSet.contains(inviteeId)) {
                results.add(result(inviteeId, "NOT_FRIEND", "친구만 초대할 수 있습니다."));
                continue;
            }

            if (alreadyMemberSet.contains(inviteeId)) {
                results.add(result(inviteeId, "ALREADY_MEMBER", "이미 파티에 가입된 사용자입니다."));
                continue;
            }

            toCreate.add(PartyInvite.builder()
                    .partyId(partyId)
                    .inviterId(inviterId)
                    .inviteeId(inviteeId)
                    .status(PartyInviteStatus.PENDING)
                    .createdAt(now)
                    .build());
        }

        if (toCreate.isEmpty()) {
            return results;
        }

        try {
            partyInviteRepository.bulkInsert(toCreate);

            for (PartyInvite invite : toCreate) {
                results.add(result(invite.getInviteeId(), "CREATED", "초대가 생성되었습니다."));
            }
        } catch (DuplicateKeyException e) {
            for (PartyInvite invite : toCreate) {
                try {
                    partyInviteRepository.insert(invite);
                    results.add(result(invite.getInviteeId(), "CREATED", "초대가 생성되었습니다."));
                } catch (DuplicateKeyException ex) {
                    results.add(result(invite.getInviteeId(), "DUPLICATE", "이미 대기 중인 초대가 존재합니다."));
                }
            }
        }

        return results;
    }

    private InviteResponsesDto.InviteResultDto result(Long inviteeId, String result, String message) {
        return InviteResponsesDto.InviteResultDto.builder()
                .inviteeId(inviteeId)
                .result(result)
                .message(message)
                .build();
    }
}