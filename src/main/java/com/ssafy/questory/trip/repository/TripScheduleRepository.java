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
    TripScheduleSnapshot findSnapshot(Long tripId, Long tripScheduleId);
    TripScheduleWsDto findWsDtoById(Long tripScheduleId);
    List<TripScheduleWsDto> findWsDtosByTripDayId(Long tripDayId);
    List<Long> findScheduleIdsByTripDayId(Long tripDayId);

    int countByTripDayId(Long tripDayId);
    boolean existsTripDayInTrip(Long tripId, Long tripDayId);

    void bulkInsert(List<TripScheduleInsertCommand> commands);
    int insert(TripScheduleInsertCommand command);

    int updateMemo(Long tripScheduleId, String memo);
    int bumpSortOrdersTemporarily(Long tripDayId, Integer tempOffset);
    int updateSortOrder(Long tripScheduleId, Integer sortOrder);
    int moveToTemporaryOrder(Long tripScheduleId, Integer temporarySortOrder);
    int bumpSortOrdersAfterDelete(Long tripDayId, Integer deletedSortOrder, Integer tempOffset);
    int normalizeSortOrdersAfterDelete(Long tripDayId, Integer tempOffset);
    int bumpIncreaseRange(Long tripDayId, Integer from, Integer to, Integer tempOffset);
    int normalizeIncreaseRange(Long tripDayId, Integer fromTemp, Integer toTemp, Integer tempOffset);
    int bumpDecreaseRange(Long tripDayId, Integer from, Integer to, Integer tempOffset);
    int normalizeDecreaseRange(Long tripDayId, Integer fromTemp, Integer toTemp, Integer tempOffset);
    int bumpIncreaseFrom(Long tripDayId, Integer fromSortOrder, Integer tempOffset);
    int normalizeIncreaseFrom(Long tripDayId, Integer fromTemp, Integer tempOffset);
    int updateTripDayAndSortOrder(Long tripScheduleId, Long tripDayId, Integer sortOrder);

    int deleteById(Long tripScheduleId);
}