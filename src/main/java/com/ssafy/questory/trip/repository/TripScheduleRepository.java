package com.ssafy.questory.trip.repository;

import com.ssafy.questory.plan.dto.ws.TripScheduleWsDto;
import com.ssafy.questory.trip.domain.TripScheduleInsertCommand;
import com.ssafy.questory.trip.domain.TripScheduleSnapshot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TripScheduleRepository {
    List<TripScheduleSnapshot> findSnapshotsByTripId(Long tripId);
    TripScheduleSnapshot findSnapshot(Long tripId, Long tripScheduleId);
    Integer findNextSortOrder(Long tripDayId);
    TripScheduleWsDto findWsDtoById(Long tripScheduleId);
    List<Long> findScheduleIdsByTripDayId(Long tripDayId);
    List<TripScheduleWsDto> findWsDtosByTripDayId(Long tripDayId);

    boolean existsTripDayInTrip(Long tripId, Long tripDayId);

    void bulkInsert(List<TripScheduleInsertCommand> commands);
    void insert(TripScheduleInsertCommand insertCommand);
    void updateMemo(Long tripScheduleId, String memo);
    void decreaseSortOrdersAfterDelete(Long oldTripDayId, Integer sortOrder);
    void bumpSortOrdersTemporarily(Long tripDayId);

    void updateSortOrder(Long scheduleId, int sortOrder);

    void deleteById(Long tripScheduleId);
}
