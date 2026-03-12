package com.ssafy.questory.trip.repository;

import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripScheduleRepository {
    List<TripScheduleSnapshot> findSnapshotsByTripId(Long tripId);

    void bulkInsert(List<TripScheduleInsertCommand> commands);
}
