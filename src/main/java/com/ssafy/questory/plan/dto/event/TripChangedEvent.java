package com.ssafy.questory.plan.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripChangedEvent<T> {
    private String eventType;
    private Long tripId;
    private Long revision;
    private Long actorMemberId;
    private String clientRequestId;
    private LocalDateTime occurredAt;
    private T payload;
}