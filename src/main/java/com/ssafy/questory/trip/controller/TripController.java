package com.ssafy.questory.trip.controller;

import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.dto.request.CreateRequestDto;
import com.ssafy.questory.trip.dto.response.CreateResponseDto;
import com.ssafy.questory.trip.service.TripService;
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
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;

    @PostMapping
    public ResponseEntity<CreateResponseDto> create(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody CreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.create(member, dto));
    }
}
