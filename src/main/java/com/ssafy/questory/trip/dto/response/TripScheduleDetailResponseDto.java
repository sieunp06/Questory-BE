package com.ssafy.questory.trip.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TripScheduleDetailResponseDto(
        @JsonProperty("trip_schedule_id")
        Long tripScheduleId,

        @JsonProperty("attraction_no")
        Integer attractionNo,

        String title,
        String memo,

        @JsonProperty("sort_order")
        Integer sortOrder,

        @JsonProperty("created_by")
        Long createdBy
) {}
