package com.ssafy.questory.trip.repository;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripPermissionRepository {
    boolean isTripParticipant(Long tripId, Long memberId);
}
