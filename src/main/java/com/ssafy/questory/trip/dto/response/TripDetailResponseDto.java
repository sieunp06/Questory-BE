package com.ssafy.questory.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record TripDetailResponseDto(
        @JsonProperty("trip_id")
        Long tripId,

        @JsonProperty("party_id")
        Long partyId,

        @JsonProperty("creator_id")
        Long creatorId,

        String title,
        String description,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate,

        List<TripDayDetailResponseDto> days
) {}
