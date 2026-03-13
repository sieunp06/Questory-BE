package com.ssafy.questory.plan.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSchedulesPayload {
    private Long tripDayId;
    private List<Long> orderedScheduleIds;
}