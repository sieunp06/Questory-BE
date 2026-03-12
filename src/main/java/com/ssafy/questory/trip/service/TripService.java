package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.party.repository.PartyMemberRepository;
import com.ssafy.questory.trip.domain.Trip;
import com.ssafy.questory.trip.dto.request.CreateRequestDto;
import com.ssafy.questory.trip.dto.response.CreateResponseDto;
import com.ssafy.questory.trip.repository.TripDayRepository;
import com.ssafy.questory.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripDayRepository tripDayRepository;
    private final PartyMemberRepository partyMemberRepository;

    @Transactional
    public CreateResponseDto create(SecurityMember member, CreateRequestDto dto) {
        LocalDate startDate = dto.startDate();
        LocalDate endDate = dto.endDate();

        validateDateRange(startDate, endDate);

        Long partyId = dto.partyId();
        Long memberId = member.getMemberId();
        if (!partyMemberRepository.exists(partyId, memberId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }

        Trip trip = Trip.builder()
                .partyId(partyId)
                .creatorId(memberId)
                .title(dto.title())
                .description(dto.description())
                .startDate(startDate)
                .endDate(endDate)
                .build();

        tripRepository.insert(trip);

        List<LocalDate> dates = startDate
                .datesUntil(endDate.plusDays(1))
                .toList();

        tripDayRepository.bulkInsert(trip.getTripId(), dates);

        return CreateResponseDto.builder()
                .tripId(trip.getTripId())
                .title(trip.getTitle())
                .description(trip.getDescription())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .build();
    }

    public void delete(SecurityMember member, Long tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new CustomException(ErrorCode.TRIP_NOT_FOUND);
        }

        Long memberId = member.getMemberId();
        if (!trip.getCreatorId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_PARTY_CREATOR_ONLY);
        }

        int deleted = tripRepository.deleteById(tripId);
        if (deleted == 0) {
            throw new CustomException(ErrorCode.TRIP_NOT_FOUND);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }
}
