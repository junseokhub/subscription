package com.api.subscription.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(int status, String message, LocalDateTime timestamp) {
        return new ErrorResponse(status, message, timestamp);
    }
}
