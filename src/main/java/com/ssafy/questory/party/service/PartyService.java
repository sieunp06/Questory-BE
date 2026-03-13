package com.ssafy.questory.party.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.party.domain.Party;
import com.ssafy.questory.party.domain.PartyMember;
import com.ssafy.questory.party.domain.PartyMemberRole;
import com.ssafy.questory.party.dto.request.CreateAndUpdateDto;
import com.ssafy.questory.party.dto.request.DelegateOwnerRequestDto;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import com.ssafy.questory.party.dto.response.PartyMemberInfoDto;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import com.ssafy.questory.party.repository.PartyRepository;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
                .role(partyMember.getRole())
                .joinedAt(now)
                .createdAt(now)
                .build();
    }

    @Transactional(readOnly = true)
    public List<PartyInfoDto> getMyParties(SecurityMember member) {
        return partyRepository.findMyParties(member.getMemberId());
    }

    @Transactional
    public void updateName(SecurityMember member, Long partyId, @Valid CreateAndUpdateDto dto) {
        Long memberId = member.getMemberId();
        String partyName = dto.name();

        validateName(partyName);

        int updated = partyRepository.updateNameIfCreator(memberId, partyId, partyName);
        if (updated == 1) return;

        validatePartyExistsAndCreator(partyId, memberId);
    }

    @Transactional
    public void delete(SecurityMember member, Long partyId) {
        Long memberId = member.getMemberId();

        int deleted = partyRepository.deleteIfCreator(memberId, partyId);
        if (deleted == 1) return;

        validatePartyExistsAndCreator(partyId, memberId);
    }

    @Transactional
    public void delegateOwner(SecurityMember member, Long partyId, @Valid DelegateOwnerRequestDto dto) {
        Long currentOwnerId = member.getMemberId();
        Long newOwnerId = dto.newOwnerId();

        if (newOwnerId == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (currentOwnerId.equals(newOwnerId)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (!partyMemberRepository.exists(partyId, newOwnerId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }

        int demoted = partyMemberRepository.demoteOwnerToMember(partyId, currentOwnerId);
        if (demoted != 1) {
            validatePartyExistsAndCreator(partyId, currentOwnerId);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        int promoted = partyMemberRepository.promoteMemberToOwner(partyId, newOwnerId);
        if (promoted != 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public List<PartyMemberInfoDto> getPartyMembers(SecurityMember member, Long partyId) {
        Long memberId = member.getMemberId();

        if (!partyMemberRepository.exists(partyId, memberId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }
        return partyMemberRepository.findMembersByPartyId(partyId);
    }

    @Transactional
    public void leave(SecurityMember member, Long partyId) {
        Long memberId = member.getMemberId();

        if (!partyMemberRepository.exists(partyId, memberId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }

        if (partyMemberRepository.isOwner(partyId, memberId)) {
            throw new CustomException(ErrorCode.PARTY_OWNER_CANNOT_LEAVE);
        }

        int deleted = partyMemberRepository.delete(partyId, memberId);
        if (deleted != 1) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validatePartyExistsAndCreator(Long partyId, Long memberId) {
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
