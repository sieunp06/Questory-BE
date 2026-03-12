package com.ssafy.questory.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record TripSummaryResponseDto(
        @JsonProperty("trip_id")
        Long tripId,

        String title,

        String description,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        @JsonProperty("creator_id")
        Long creatorId
) {}
