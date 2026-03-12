package com.ssafy.questory.trip.repository;

import com.ssafy.questory.trip.domain.Trip;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripRepository {
    Trip findById(Long tripId);

    void insert(Trip trip);

    void updateInfo(Trip update);
}
