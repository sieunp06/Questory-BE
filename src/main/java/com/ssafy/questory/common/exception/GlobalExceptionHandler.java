package com.ssafy.questory.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseDto> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponseDto.of(code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ErrorCode code = ErrorCode.INVALID_REQUEST;

        List<ErrorResponseDto.FieldErrorDto> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorDto)
                .toList();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponseDto.of(code, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        ErrorCode code = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponseDto.of(code));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnexpected(Exception e) {
        ErrorCode code = ErrorCode.INTERNAL_SERVER_ERROR;
        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponseDto.of(code));
    }

    private ErrorResponseDto.FieldErrorDto toFieldErrorDto(FieldError err) {
        String message = (err.getDefaultMessage() != null) ? err.getDefaultMessage() : "잘못된 요청입니다.";
        return new ErrorResponseDto.FieldErrorDto(err.getField(), message);
    }
}
