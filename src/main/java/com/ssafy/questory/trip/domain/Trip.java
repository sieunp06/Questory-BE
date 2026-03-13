package com.ssafy.questory.trip.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class Trip {
    private Long tripId;
    private Long partyId;
    private Long creatorId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
