package com.ssafy.questory.plan.dto.ws;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TripScheduleWsDto {
    private Long tripScheduleId;
    private Long tripDayId;
    private Integer attractionNo;
    private String title;
    private String memo;
    private Integer sortOrder;
}