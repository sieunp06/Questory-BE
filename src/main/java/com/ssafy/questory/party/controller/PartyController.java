package com.ssafy.questory.party.controller;

import com.ssafy.questory.party.dto.request.CreateAndUpdateDto;
import com.ssafy.questory.party.dto.response.PartyInfoDto;
import com.ssafy.questory.party.service.PartyService;
import com.ssafy.questory.member.domain.SecurityMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
