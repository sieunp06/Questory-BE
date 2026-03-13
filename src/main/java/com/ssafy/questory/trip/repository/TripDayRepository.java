package com.ssafy.questory.trip.repository;

import com.ssafy.questory.trip.domain.TripDay;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TripDayRepository {
    List<TripDay> findByTripId(Long tripId);

    void bulkInsert(Long tripId, List<LocalDate> dates);

    int deleteByTripId(Long tripId);
}
