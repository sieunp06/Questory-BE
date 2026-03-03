package com.ssafy.questory.party.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.party.dto.request.CreateAndUpdateDto;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import com.ssafy.questory.party.service.PartyService;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/party")
@RequiredArgsConstructor
public class PartyController {
    private final PartyService partyService;

    @PostMapping
    public ResponseEntity<PartyInfoDto> create(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody CreateAndUpdateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partyService.create(member, dto));
    }

    @PatchMapping("/{partyId}")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long partyId,
            @Valid @RequestBody CreateAndUpdateDto dto){
        partyService.updateName(member, partyId, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("파티 이름 변경에 성공했습니다."));
    }

    @DeleteMapping("/{partyId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long partyId) {
        partyService.delete(member, partyId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("파티 삭제가 완료되었습니다."));
    }
}
