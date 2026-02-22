package com.ssafy.questory.common.api;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        String code,
        Instant timestamp,
        List<FieldErrorItem> errors
) {
    public static ErrorResponse of(String message, String code, List<FieldErrorItem> errors) {
        return new ErrorResponse(false, message, code, Instant.now(), errors);
    }

    public record FieldErrorItem(
            String field,
            String reason
    ) {}
}
