package com.team04.playlist.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {} - {}", errorCode.name(), e.getMessage());
        return toResponse(errorCode, e.getMessage());
    }

    // @Valid @RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    // @PathVariable, @RequestParam 등 메서드 파라미터 검증 실패
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException e) {
        String message = e.getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, message);
    }

    // JSON 파싱 실패, 타입 불일치, 필수 파라미터 누락
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn("Bad request: {}", e.getMessage());
        return toResponse(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getMessage());
    }

    // 그 외 예상하지 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return toResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode, message));
    }
}
