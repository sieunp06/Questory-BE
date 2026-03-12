package com.ssafy.questory.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record TripDayDetailResponseDto(
        @JsonProperty("trip_day_id")
        Long tripDayId,

        @JsonProperty("day_num")
        Integer dayNum,

        @JsonProperty("trip_date")
        LocalDate tripDate,

        List<TripScheduleDetailResponseDto> schedules
) {}
