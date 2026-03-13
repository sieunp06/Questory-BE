package com.ssafy.questory.plan.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TripChangedAfterCommitEvent {
    private final Long tripId;
    private final Object payload;
}