package com.revplay.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ============================================================
// CUSTOM EXCEPTION CLASSES
// ============================================================

/**
 * Thrown when a requested resource is not found in the database.
 * Maps to HTTP 404 Not Found.
 *
 * Usage: throw new ResourceNotFoundException("Song", "id", 42L);
 *        → "Song not found with id: 42"
 */
class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName    = fieldName;
        this.fieldValue   = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName()    { return fieldName; }
    public Object getFieldValue()   { return fieldValue; }
}

/**
 * Thrown when a create/update operation violates a uniqueness rule.
 * Maps to HTTP 409 Conflict.
 *
 * Usage: throw new DuplicateResourceException("Email already registered: alice@mail.com");
 */
class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

/**
 * Thrown when a user attempts an action they are not permitted to perform.
 * Maps to HTTP 403 Forbidden.
 *
 * Usage: throw new UnauthorizedAccessException("You do not have permission to delete this playlist.");
 */
class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

/**
 * Thrown when the client sends a request that violates a business rule
 * not covered by Bean Validation annotations.
 * Maps to HTTP 400 Bad Request.
 *
 * Usage: throw new BadRequestException("Cannot delete an album that still has songs.");
 */
class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

// ============================================================
// ERROR RESPONSE DTO
// ============================================================

/**
 * Standard error response body returned for all exceptions.
 *
 * Example JSON:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Song not found with id: 99",
 *   "path": "/api/songs/99",
 *   "timestamp": "2024-06-01 14:32:00"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /** Populated only for @Valid failures — lists each field-level error */
    private List<FieldError> fieldErrors;

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

    /** Inner class for field-level validation errors */
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

    public int              getStatus()      { return status; }
    public void             setStatus(int status) { this.status = status; }
    public String           getError()       { return error; }
    public void             setError(String error) { this.error = error; }
    public String           getMessage()     { return message; }
    public void             setMessage(String message) { this.message = message; }
    public String           getPath()        { return path; }
    public void             setPath(String path) { this.path = path; }
    public LocalDateTime    getTimestamp()   { return timestamp; }
    public void             setTimestamp(LocalDateTime ts) { this.timestamp = ts; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }
    public void             setFieldErrors(List<FieldError> fe) { this.fieldErrors = fe; }
}

// ============================================================
// GLOBAL EXCEPTION HANDLER
// ============================================================

/**
 * Centralized exception handler for all RevPlay REST API controllers.
 *
 * Intercepts exceptions thrown from controllers and services,
 * logs them at the appropriate level, and returns a consistent
 * ErrorResponse JSON body with the correct HTTP status code.
 *
 * Logging standard:
 *   ERROR — unhandled server-side failures (5xx)
 *   WARN  — client errors / access violations (4xx)
 *   DEBUG — expected business exceptions (not found, duplicate)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ─────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.debug("Resource not found: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found",
                ex.getMessage(), request.getRequestURI()
        ));
    }

    // ── 409 Conflict ──────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict",
                ex.getMessage(), request.getRequestURI()
        ));
    }

    // ── 403 Forbidden ─────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {

        log.warn("Unauthorized access: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), "Forbidden",
                ex.getMessage(), request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Business Rule ───────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {

        log.warn("Bad request: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                ex.getMessage(), request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Bean Validation (@Valid) ─────────────────

    /**
     * Handles @Valid / @Validated failures on request DTOs.
     * Returns a list of field-level errors in the response body.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "Input validation failed. See fieldErrors for details.",
     *   "fieldErrors": [
     *     { "field": "email", "rejectedValue": "not-an-email", "message": "must be a valid email" }
     *   ]
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null,
                        fe.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed: {} field error(s) | path={}", fieldErrors.size(), request.getRequestURI());

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "Input validation failed. See fieldErrors for details.",
                request.getRequestURI()
        );
        body.setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 400 Bad Request — Malformed JSON ──────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body | path={}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                "Request body is malformed or missing.",
                request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Missing Query Parameter ─────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing request parameter: {} | path={}", ex.getParameterName(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                String.format("Required parameter '%s' is missing.", ex.getParameterName()),
                request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Path Variable / Param Type Mismatch ─────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'.",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("Type mismatch: {} | path={}", message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                message, request.getRequestURI()
        ));
    }

    // ── 413 Payload Too Large — File Upload ───────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("Upload size exceeded | path={}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ErrorResponse(
                HttpStatus.PAYLOAD_TOO_LARGE.value(), "Payload Too Large",
                "File size exceeds the maximum allowed upload limit.",
                request.getRequestURI()
        ));
    }

    // ── 500 Internal Server Error — Catch-All ─────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at path={} | {}: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        ));
    }
}