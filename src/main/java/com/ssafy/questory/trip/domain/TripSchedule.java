package com.ssafy.questory.trip.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TripSchedule {
    private Long tripScheduleId;
    private Long tripDayId;
    private Integer attractionNo;
    private String title;
    private String memo;
    private Integer sortOrder;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
