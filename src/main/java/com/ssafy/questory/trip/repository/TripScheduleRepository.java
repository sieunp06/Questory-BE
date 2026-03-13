package com.ssafy.questory.trip.repository;

import com.ssafy.questory.plan.dto.ws.TripScheduleWsDto;
import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripScheduleRepository {
    List<TripScheduleSnapshot> findSnapshotsByTripId(Long tripId);
    Integer findNextSortOrder(Long tripDayId);
    TripScheduleWsDto findWsDtoById(Long tripScheduleId);

    boolean existsTripDayInTrip(Long tripId, Long tripDayId);

    void bulkInsert(List<TripScheduleInsertCommand> commands);
    void insert(TripScheduleInsertCommand insertCommand);
}
