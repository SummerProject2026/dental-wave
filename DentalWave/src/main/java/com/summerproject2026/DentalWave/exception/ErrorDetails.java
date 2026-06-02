package com.summerproject2026.DentalWave.exception;

import java.time.LocalDateTime;

// Contains details about an error to send back to the client
public class ErrorDetails {

    // When the error occurred
    private LocalDateTime timestamp;

    // Human-readable error message
    private String message;

    // The request path that caused the error
    private String path;

    public ErrorDetails(LocalDateTime timestamp, String message, String path) {
        this.timestamp = timestamp;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
}