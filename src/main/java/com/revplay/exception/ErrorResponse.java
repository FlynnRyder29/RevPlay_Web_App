package com.revplay.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response body returned by GlobalExceptionHandler
 * for all API errors.
 *
 * Example JSON (validation error):
 * {
 *   "timestamp": "2024-06-01 14:32:00",
 *   "status": 400,
 *   "error": "Validation Failed",
 *   "message": "Input validation failed. See fieldErrors for details.",
 *   "path": "/api/songs",
 *   "fieldErrors": [
 *     { "field": "email", "rejectedValue": "not-an-email", "message": "must be a valid email address" }
 *   ]
 * }
 *
 * Example JSON (not found):
 * {
 *   "timestamp": "2024-06-01 14:32:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Song not found with id: 99",
 *   "path": "/api/songs/99"
 * }
 */
@JsonPropertyOrder({ "timestamp", "status", "error", "message", "path", "fieldErrors" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    /** Populated only for @Valid failures — one entry per invalid field */
    private List<FieldError> fieldErrors;

    // ── Constructors ──────────────────────────────────────────────

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status  = status;
        this.error   = error;
        this.message = message;
        this.path    = path;
    }

    // ── Inner class for field-level validation errors ─────────────

    /**
     * Represents a single field validation failure.
     * rejectedValue lets the frontend show the user exactly what they submitted.
     */
    @JsonPropertyOrder({ "field", "rejectedValue", "message" })
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;

        public FieldError(String field, String rejectedValue, String message) {
            this.field         = field;
            this.rejectedValue = rejectedValue;
            this.message       = message;
        }

        public String getField()         { return field; }
        public String getRejectedValue() { return rejectedValue; }
        public String getMessage()       { return message; }
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public LocalDateTime    getTimestamp()   { return timestamp; }
    public void             setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int              getStatus()      { return status; }
    public void             setStatus(int status) { this.status = status; }

    public String           getError()       { return error; }
    public void             setError(String error) { this.error = error; }

    public String           getMessage()     { return message; }
    public void             setMessage(String message) { this.message = message; }

    public String           getPath()        { return path; }
    public void             setPath(String path) { this.path = path; }

    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void             setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }
}