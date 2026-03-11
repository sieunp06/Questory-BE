package com.ssafy.questory.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateResponseDto(
        @JsonProperty("trip_id")
        Long tripId,

        String title,


        String description,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("end_date")
        LocalDate endDate
) {}
