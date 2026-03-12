package com.ssafy.questory.trip.controller;

import com.ssafy.questory.common.api.ApiResponse;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.dto.request.CreateRequestDto;
import com.ssafy.questory.trip.dto.request.UpdateRequestDto;
import com.ssafy.questory.trip.dto.response.CreateResponseDto;
import com.ssafy.questory.trip.service.TripService;
import com.ssafy.questory.trip.service.TripUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    private final TripUpdateService tripUpdateService;

    @PostMapping
    public ResponseEntity<CreateResponseDto> create(
            @AuthenticationPrincipal SecurityMember member,
            @Valid @RequestBody CreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.create(member, dto));
    }

    @PatchMapping("/{tripId}")
    public ResponseEntity<ApiResponse<Void>> updateInfo(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long tripId,
            @Valid @RequestBody UpdateRequestDto dto) {
        tripUpdateService.updateInfo(member, tripId, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok("여행 기본 정보 수정에 성공했습니다."));
    }
}
