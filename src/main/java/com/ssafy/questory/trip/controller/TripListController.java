package com.ssafy.questory.trip.controller;

import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.dto.response.TripDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripSummaryResponseDto;
import com.ssafy.questory.trip.service.TripListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parties/{partyId}/trips")
@RequiredArgsConstructor
public class TripListController {
    private final TripListService tripListService;

    @GetMapping
    public ResponseEntity<List<TripSummaryResponseDto>> getTripsByParty(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long partyId) {
        return ResponseEntity.status(HttpStatus.OK).body(tripListService.getTripsByParty(member, partyId));
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<TripDetailResponseDto> getTripDetail(
            @AuthenticationPrincipal SecurityMember member,
            @PathVariable Long partyId,
            @PathVariable Long tripId) {
        return ResponseEntity.status(HttpStatus.OK).body(tripListService.getTripDetail(member, partyId, tripId));
    }
}
