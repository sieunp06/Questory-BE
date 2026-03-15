package com.ssafy.questory.plan.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveSchedulePayload {
    private Long tripScheduleId;
    private Long targetTripDayId;
    private Integer targetOrder;
}