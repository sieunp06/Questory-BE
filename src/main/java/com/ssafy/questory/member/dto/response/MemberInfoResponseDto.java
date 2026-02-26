package com.ssafy.questory.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record MemberInfoResponseDto(
        String email,
        String nickname,

        @JsonProperty("total_exp")
        Long totalExp,

        @JsonProperty("representative_title")
        String representativeTitle
) {}
