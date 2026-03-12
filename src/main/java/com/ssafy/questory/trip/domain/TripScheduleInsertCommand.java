package com.ssafy.questory.trip.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripScheduleInsertCommand {
    private Long tripDayId;
    private Integer attractionNo;
    private String title;
    private String memo;
    private Integer sortOrder;
    private Long createdBy;
}