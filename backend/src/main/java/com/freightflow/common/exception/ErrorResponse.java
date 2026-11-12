package com.freightflow.common.exception;

import java.time.Instant;

/**
 * Standard error payload returned by the API.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String error, String code, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, code, message, path);
    }
}
