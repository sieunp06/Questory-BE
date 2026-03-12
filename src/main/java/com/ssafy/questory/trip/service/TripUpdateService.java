package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.domain.Trip;
import com.ssafy.questory.trip.domain.TripDay;
import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import com.ssafy.questory.trip.dto.request.UpdateRequestDto;
import com.ssafy.questory.trip.repository.TripDayRepository;
import com.ssafy.questory.trip.repository.TripRepository;
import com.ssafy.questory.trip.repository.TripScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripUpdateService {
    private final TripRepository tripRepository;
    private final TripDayRepository tripDayRepository;
    private final TripScheduleRepository tripScheduleRepository;;

    @Transactional
    public void updateInfo(SecurityMember member, Long tripId, UpdateRequestDto dto) {
        validateDateRange(dto.startDate(), dto.endDate());

        Trip trip = getTripOrThrow(tripId);
        validateTripOwner(member.getMemberId(), trip);

        LocalDate newStartDate = dto.startDate();
        LocalDate newEndDate = dto.endDate();

        List<TripScheduleSnapshot> snapshots = tripScheduleRepository.findSnapshotsByTripId(tripId);

        updateTrip(trip, dto);
        recreateTripDays(tripId, newStartDate, newEndDate);

        Map<LocalDate, Long> tripDayIdByDate = getTripDayIdByDate(tripId);
        List<TripScheduleInsertCommand> commands =
                buildScheduleInsertCommands(snapshots, newStartDate, newEndDate, tripDayIdByDate);

        if (!commands.isEmpty()) {
            tripScheduleRepository.bulkInsert(commands);
        }
    }

    private void updateTrip(Trip trip, UpdateRequestDto dto) {
        Trip update = Trip.builder()
                .tripId(trip.getTripId())
                .partyId(trip.getPartyId())
                .creatorId(trip.getCreatorId())
                .title(dto.title())
                .description(dto.description())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .build();

        tripRepository.updateInfo(update);
    }

    private void recreateTripDays(Long tripId, LocalDate startDate, LocalDate endDate) {
        tripDayRepository.deleteByTripId(tripId);

        List<LocalDate> dates = startDate.datesUntil(endDate.plusDays(1)).toList();
        tripDayRepository.bulkInsert(tripId, dates);
    }

    private Map<LocalDate, Long> getTripDayIdByDate(Long tripId) {
        return tripDayRepository.findByTripId(tripId).stream()
                .collect(Collectors.toMap(
                        TripDay::getTripDate,
                        TripDay::getTripDayId,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private List<TripScheduleInsertCommand> buildScheduleInsertCommands(
            List<TripScheduleSnapshot> snapshots,
            LocalDate newStartDate,
            LocalDate newEndDate,
            Map<LocalDate, Long> tripDayIdByDate) {
        Map<LocalDate, List<TripScheduleSnapshot>> grouped = groupSnapshotsByTargetDate(
                snapshots, newStartDate, newEndDate, tripDayIdByDate
        );

        List<TripScheduleInsertCommand> commands = new ArrayList<>();

        for (Map.Entry<LocalDate, List<TripScheduleSnapshot>> entry : grouped.entrySet()) {
            LocalDate targetDate = entry.getKey();
            List<TripScheduleSnapshot> sameDateSchedules = entry.getValue();

            sameDateSchedules.sort(Comparator
                    .comparing(TripScheduleSnapshot::getOldTripDate)
                    .thenComparing(TripScheduleSnapshot::getSortOrder)
                    .thenComparing(TripScheduleSnapshot::getTripScheduleId));

            Long tripDayId = tripDayIdByDate.get(targetDate);

            for (int i = 0; i < sameDateSchedules.size(); i++) {
                TripScheduleSnapshot snapshot = sameDateSchedules.get(i);

                commands.add(TripScheduleInsertCommand.builder()
                        .tripDayId(tripDayId)
                        .attractionNo(snapshot.getAttractionNo())
                        .title(snapshot.getTitle())
                        .memo(snapshot.getMemo())
                        .sortOrder(i + 1)
                        .createdBy(snapshot.getCreatedBy())
                        .build());
            }
        }

        return commands;
    }

    private Map<LocalDate, List<TripScheduleSnapshot>> groupSnapshotsByTargetDate(
            List<TripScheduleSnapshot> snapshots,
            LocalDate newStartDate,
            LocalDate newEndDate,
            Map<LocalDate, Long> tripDayIdByDate
    ) {
        Map<LocalDate, List<TripScheduleSnapshot>> grouped = new LinkedHashMap<>();

        for (LocalDate tripDate : tripDayIdByDate.keySet()) {
            grouped.put(tripDate, new ArrayList<>());
        }

        for (TripScheduleSnapshot snapshot : snapshots) {
            LocalDate targetDate = clamp(snapshot.getOldTripDate(), newStartDate, newEndDate);
            grouped.get(targetDate).add(snapshot);
        }

        return grouped;
    }

    private LocalDate clamp(LocalDate date, LocalDate min, LocalDate max) {
        if (date.isBefore(min)) {
            return min;
        }
        if (date.isAfter(max)) {
            return max;
        }
        return date;
    }

    private Trip getTripOrThrow(Long tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new CustomException(ErrorCode.TRIP_NOT_FOUND);
        }
        return trip;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateTripOwner(Long memberId, Trip trip) {
        if (!trip.getCreatorId().equals(memberId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_PARTY_CREATOR_ONLY);
        }
    }
}
