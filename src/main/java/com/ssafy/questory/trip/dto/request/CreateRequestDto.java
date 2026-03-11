package com.ssafy.questory.trip.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateRequestDto(
        @JsonProperty("party_id")
        @NotNull
        Long partyId,

        @NotBlank
        @Size(max = 100)
        String title,

        @Size(max = 1000)
        String description,

        @JsonProperty("start_date")
        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        @JsonProperty("end_date")
        @NotNull
        @FutureOrPresent
        LocalDate endDate
) {}
