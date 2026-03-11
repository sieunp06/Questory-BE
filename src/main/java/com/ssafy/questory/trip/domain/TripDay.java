package com.ssafy.questory.trip.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TripDay {
    private Long tripDayId;
    private Long tripId;
    private Integer dayNum;
    private LocalDate tripDate;
    private LocalDateTime createdAt;
}
