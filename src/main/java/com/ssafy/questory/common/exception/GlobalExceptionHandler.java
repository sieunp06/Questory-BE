package com.ssafy.questory.common.exception;

import org.springframework.http.ResponseEntity;
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
                .map(err -> new ErrorResponseDto.FieldErrorDto(
                        err.getField(),
                        err.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity
                .status(code.getStatus())
                .body(ErrorResponseDto.of(code, errors));
    }
}
