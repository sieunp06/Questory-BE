package com.ssafy.questory.plan.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSchedulePayload {
    private Long tripDayId;
    private Integer attractionNo;
    private String title;
    private String memo;
}
