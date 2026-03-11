package com.ssafy.questory.trip.repository;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TripDayRepository {
    void bulkInsert(Long tripId, List<LocalDate> dates);
}
