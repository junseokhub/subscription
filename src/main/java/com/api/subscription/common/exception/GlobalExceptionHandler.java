package com.api.subscription.common.exception;

import com.api.subscription.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of(
                e.getErrorCode().getHttpStatus().value(),
                e.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.fail(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("ValidationException: {}", message);
        ErrorResponse error = ErrorResponse.of(400, message, LocalDateTime.now());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(error));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(RestClientException e) {
        log.error("외부 API 호출 실패: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.of(503, ErrorCode.EXTERNAL_API_FAILURE.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        ErrorResponse error = ErrorResponse.of(500, ErrorCode.INTERNAL_SERVER_ERROR.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(error));
    }
    
}