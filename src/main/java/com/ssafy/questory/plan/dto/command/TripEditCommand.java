package com.ssafy.questory.plan.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripEditCommand<T> {
    private String type;
    private Long tripId;
    private Long baseRevision;
    private String clientRequestId;
    private T payload;
}
