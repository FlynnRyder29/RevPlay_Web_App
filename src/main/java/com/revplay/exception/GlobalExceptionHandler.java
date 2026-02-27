package com.revplay.exception;

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ============================================================
// GLOBAL EXCEPTION HANDLER
// ============================================================

/**
 * Centralized exception handler for all RevPlay REST API controllers.
 *
 * Logging standard:
 *   WARN  — all 4xx client errors
 *   ERROR — 5xx unhandled server errors (with stack trace)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ─────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status",  404,
                "error",   "Not Found",
                "message", ex.getMessage(),
                "path",    request.getRequestURI()
        ));
    }

    // ── 409 Conflict ──────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {

        log.warn("Duplicate resource: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status",  409,
                "error",   "Conflict",
                "message", ex.getMessage(),
                "path",    request.getRequestURI()
        ));
    }

    // ── 403 Forbidden ─────────────────────────────────────────────

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, HttpServletRequest request) {

        log.warn("Unauthorized access: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status",  403,
                "error",   "Forbidden",
                "message", ex.getMessage(),
                "path",    request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Business Rule ───────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex, HttpServletRequest request) {

        log.warn("Bad request: {} | path={}", ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",  400,
                "error",   "Bad Request",
                "message", ex.getMessage(),
                "path",    request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — @Valid Failures ─────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<Map<String, String>> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field",   fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed: {} error(s) | path={}", fieldErrors.size(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",      400,
                "error",       "Validation Failed",
                "message",     "Input validation failed. See fieldErrors for details.",
                "path",        request.getRequestURI(),
                "fieldErrors", fieldErrors
        ));
    }

    // ── 400 Bad Request — Malformed JSON ──────────────────────────

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        log.warn("Malformed request body | path={}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",  400,
                "error",   "Bad Request",
                "message", "Request body is malformed or missing.",
                "path",    request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Missing Query Parameter ─────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        log.warn("Missing parameter: {} | path={}", ex.getParameterName(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",  400,
                "error",   "Bad Request",
                "message", String.format("Required parameter '%s' is missing.", ex.getParameterName()),
                "path",    request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Path Variable Type Mismatch ─────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String message = String.format("Parameter '%s' should be of type '%s'.",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        log.warn("Type mismatch: {} | path={}", message, request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status",  400,
                "error",   "Bad Request",
                "message", message,
                "path",    request.getRequestURI()
        ));
    }

    // ── 413 Payload Too Large ──────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {

        log.warn("Upload size exceeded | path={}", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "status",  413,
                "error",   "Payload Too Large",
                "message", "File size exceeds the maximum allowed upload limit.",
                "path",    request.getRequestURI()
        ));
    }

    // ── 500 Internal Server Error — Catch-All ─────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception at path={} | {}: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status",  500,
                "error",   "Internal Server Error",
                "message", "An unexpected error occurred. Please try again later.",
                "path",    request.getRequestURI()
        ));
    }
}