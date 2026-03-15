package com.ssafy.questory.trip.repository;

import com.ssafy.questory.trip.domain.Trip;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripRepository {
    Trip findById(Long tripId);
    Long findRevisionById(Long tripId);

    void insert(Trip trip);

    void updateInfo(Trip update);
    Long increaseRevision(Long tripId);

    int deleteById(Long tripId);
}
