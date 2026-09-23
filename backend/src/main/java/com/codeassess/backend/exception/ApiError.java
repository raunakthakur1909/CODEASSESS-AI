package com.codeassess.backend.exception;

import java.time.LocalDateTime;

public class ApiError {

    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    public ApiError(
            int status,
            String message,
            LocalDateTime timestamp
    ) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
