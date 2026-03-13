package com.ssafy.questory.trip.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class TripScheduleSnapshot {
    private Long tripScheduleId;
    private Long oldTripDayId;
    private LocalDate oldTripDate;

    private Integer attractionNo;
    private String title;
    private String memo;
    private Integer sortOrder;
    private Long createdBy;
}
