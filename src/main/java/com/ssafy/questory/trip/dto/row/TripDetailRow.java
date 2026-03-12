package com.ssafy.questory.trip.dto.row;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class TripDetailRow {
    private Long tripId;
    private Long partyId;
    private Long creatorId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long tripDayId;
    private Integer dayNum;
    private LocalDate tripDate;

    private Long tripScheduleId;
    private Integer attractionNo;
    private String scheduleTitle;
    private String memo;
    private Integer sortOrder;
    private Long createdBy;
}
