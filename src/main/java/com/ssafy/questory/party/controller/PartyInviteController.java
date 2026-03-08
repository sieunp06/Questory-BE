package com.ssafy.questory.party.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.dto.request.InviteRequestDto;
import com.ssafy.questory.party.dto.response.InviteResponsesDto;
import com.ssafy.questory.party.service.PartyInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party/invite")
@RequiredArgsConstructor
public class PartyInviteController {
    private final PartyInviteService partyInviteService;

    @PostMapping("/{partyId}")
    public ResponseEntity<InviteResponsesDto> invite(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long partyId,
            @Valid @RequestBody InviteRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partyInviteService.invite(member, partyId, dto));
    }

    @PatchMapping("/{inviteId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long inviteId) {
        partyInviteService.cancel(member, inviteId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("파티 초대를 취소했습니다."));
    }
}
