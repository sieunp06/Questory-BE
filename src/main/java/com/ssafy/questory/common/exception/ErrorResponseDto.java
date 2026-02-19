package com.ssafy.questory.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ErrorResponseDto {

    private int status;
    private String message;
    private List<FieldErrorDto> errors;

    public static ErrorResponseDto of(ErrorCode errorCode) {
        return ErrorResponseDto.builder()
                .status(errorCode.getStatusCode())
                .message(errorCode.getMessage())
                .build();
    }

    public static ErrorResponseDto of(ErrorCode errorCode, List<FieldErrorDto> errors) {
        return ErrorResponseDto.builder()
                .status(errorCode.getStatusCode())
                .message(errorCode.getMessage())
                .errors(errors)
                .build();
    }

    @Getter
    @Builder
    public static class FieldErrorDto {
        private String field;
        private String reason;
    }
}
