package com.ssafy.questory.trip.repository;

import com.ssafy.questory.trip.dto.response.TripSummaryResponseDto;
import com.ssafy.questory.trip.dto.row.TripDetailRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripListRepository {
    boolean existsPartyMember(Long partyId, Long memberId);

    List<TripSummaryResponseDto> findTripsByPartyId(Long partyId);

    List<TripDetailRow> findTripDetailRows(Long partyId, Long tripId);
}
