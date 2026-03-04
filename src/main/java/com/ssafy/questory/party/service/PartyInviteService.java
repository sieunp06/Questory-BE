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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        List results = new ArrayList<InviteResponsesDto.InviteResultDto>();
        for (Long inviteeId : invitees) {
            boolean isFriend = friendRepository.existsFriend(inviterId, inviteeId);
            if (!isFriend) {
                results.add(result(inviteeId, "NOT_FRIEND", "친구만 초대할 수 있습니다."));
                continue;
            }

            boolean isAlreadyMember = partyMemberRepository.existsActiveMember(partyId, inviteeId);
            if (isAlreadyMember) {
                results.add(result(inviteeId, "ALREADY_MEMBER", "이미 파티에 가입된 사용자입니다."));
                continue;
            }

            PartyInvite partyInvite = PartyInvite.builder()
                    .partyId(partyId)
                    .inviterId(inviterId)
                    .inviteeId(inviteeId)
                    .status(PartyInviteStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            try {
                partyInviteRepository.insert(partyInvite);
                results.add(result(inviteeId, "CREATED", "초대가 생성되었습니다."));
            } catch (DuplicateKeyException e) {
                results.add(result(inviteeId, "DUPLICATE", "이미 대기 중인 초대가 존재합니다."));
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
