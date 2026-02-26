package com.ssafy.questory.title.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Title {
    private Long titleId;
    private String name;
}
