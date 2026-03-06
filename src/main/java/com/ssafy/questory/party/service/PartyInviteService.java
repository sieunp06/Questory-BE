package com.ssafy.questory.party.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.friend.repository.FriendRepository;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.domain.PartyInvite;
import com.ssafy.questory.party.domain.PartyInviteStatus;
import com.ssafy.questory.party.dto.request.InviteRequestDto;
import com.ssafy.questory.party.dto.response.InviteResponsesDto;
import com.ssafy.questory.party.repository.PartyInviteRepository;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PartyInviteService {
    private final PartyMemberRepository partyMemberRepository;
    private final PartyInviteRepository partyInviteRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public InviteResponsesDto invite(SecurityMember member, Long partyId, @Valid InviteRequestDto dto) {
        Long inviterId = member.getMemberId();
        List<Long> inviteesId = dto.inviteeIds();

        if (!partyMemberRepository.exists(partyId, inviterId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }

        Set<Long> invitees = new LinkedHashSet<>();
        for (Long id : inviteesId) {
            if (id == null) continue;
            if (inviterId.equals(id)) continue;
            invitees.add(id);
        }

        List<Long> targets = new ArrayList<>(invitees);
        Set<Long> friendSet = new HashSet<>(
                friendRepository.findFriendIdsAmong(inviterId, targets)
        );

        Set<Long> alreadyMemberSet = new HashSet<>(
                partyMemberRepository.findExistingMemberIds(partyId, targets)
        );

        List<InviteResponsesDto.InviteResultDto> results = new ArrayList<>();

        for (Long inviteeId : targets) {
            if (!friendSet.contains(inviteeId)) {
                results.add(InviteResponsesDto.InviteResultDto.builder()
                        .inviteeId(inviteeId)
                        .result("NOT_FRIEND")
                        .message("친구만 초대할 수 있습니다.")
                        .build());
                continue;
            }

            if (alreadyMemberSet.contains(inviteeId)) {
                results.add(InviteResponsesDto.InviteResultDto.builder()
                        .inviteeId(inviteeId)
                        .result("ALREADY_MEMBER")
                        .message("이미 파티에 가입된 사용자입니다.")
                        .build());
                continue;
            }

            PartyInvite invite = PartyInvite.builder()
                    .partyId(partyId)
                    .inviterId(inviterId)
                    .inviteeId(inviteeId)
                    .status(PartyInviteStatus.PENDING)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            try {
                partyInviteRepository.insert(invite);
                results.add(InviteResponsesDto.InviteResultDto.builder()
                        .inviteeId(inviteeId)
                        .result("CREATED")
                        .message("초대가 생성되었습니다.")
                        .build());
            } catch (DuplicateKeyException e) {
                results.add(InviteResponsesDto.InviteResultDto.builder()
                        .inviteeId(inviteeId)
                        .result("DUPLICATE")
                        .message("이미 대기 중인 초대가 존재합니다.")
                        .build());
            }
        }

        return InviteResponsesDto.builder()
                .results(results)
                .build();
    }

    private InviteResponsesDto.InviteResultDto result(Long inviteeId, String result, String message) {
        return InviteResponsesDto.InviteResultDto.builder()
                .inviteeId(inviteeId)
                .result(result)
                .message(message)
                .build();
    }
}
