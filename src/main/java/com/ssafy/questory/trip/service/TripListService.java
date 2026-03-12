package com.ssafy.questory.trip.service;

import com.ssafy.questory.common.exception.CustomException;
import com.ssafy.questory.common.exception.ErrorCode;
import com.ssafy.questory.member.domain.SecurityMember;
import com.ssafy.questory.trip.dto.response.TripDayDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripScheduleDetailResponseDto;
import com.ssafy.questory.trip.dto.response.TripSummaryResponseDto;
import com.ssafy.questory.trip.dto.row.TripDetailRow;
import com.ssafy.questory.trip.repository.TripListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripListService {
    private final TripListRepository tripListRepository;

    public List<TripSummaryResponseDto> getTripsByParty(SecurityMember member, Long partyId) {
        validatePartyMember(partyId, member.getMemberId());
        return tripListRepository.findTripsByPartyId(partyId);
    }

    public TripDetailResponseDto getTripDetail(SecurityMember member, Long partyId, Long tripId) {
        validatePartyMember(partyId, member.getMemberId());

        List<TripDetailRow> rows = tripListRepository.findTripDetailRows(partyId, tripId);
        if (rows.isEmpty()) {
            throw new CustomException(ErrorCode.TRIP_NOT_FOUND);
        }

        TripDetailRow first = rows.get(0);

        Map<Long, TripDayDetailResponseDto> dayMap = new LinkedHashMap<>();

        for (TripDetailRow row : rows) {
            if (row.getTripDayId() == null) {
                continue;
            }

            dayMap.computeIfAbsent(
                    row.getTripDayId(),
                    id -> new TripDayDetailResponseDto(
                            row.getTripDayId(),
                            row.getDayNum(),
                            row.getTripDate(),
                            new ArrayList<>()
                    )
            );

            if (row.getTripScheduleId() != null) {
                dayMap.get(row.getTripDayId()).schedules().add(
                        new TripScheduleDetailResponseDto(
                                row.getTripScheduleId(),
                                row.getAttractionNo(),
                                row.getScheduleTitle(),
                                row.getMemo(),
                                row.getSortOrder(),
                                row.getCreatedBy()
                        )
                );
            }
        }

        return new TripDetailResponseDto(
                first.getTripId(),
                first.getPartyId(),
                first.getCreatorId(),
                first.getTitle(),
                first.getDescription(),
                first.getStartDate(),
                first.getEndDate(),
                new ArrayList<>(dayMap.values())
        );
    }

    private void validatePartyMember(Long partyId, Long memberId) {
        if (!tripListRepository.existsPartyMember(partyId, memberId)) {
            throw new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND);
        }
    }
}
