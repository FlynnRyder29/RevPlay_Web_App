package com.revplay.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// ============================================================
// SPRING SECURITY — JSON ERROR HANDLERS
//
// Spring Security exceptions (AccessDeniedException,
// AuthenticationException) bypass @RestControllerAdvice entirely.
// These two @Component beans intercept them at the filter chain
// level and return the same consistent JSON as ErrorResponse.
//
// Wire them in SecurityConfig:
//
//   @Autowired RevPlayAuthenticationEntryPoint authEntryPoint;
//   @Autowired RevPlayAccessDeniedHandler      accessDeniedHandler;
//
//   http.exceptionHandling(ex -> ex
//       .authenticationEntryPoint(authEntryPoint)
//       .accessDeniedHandler(accessDeniedHandler));
// ============================================================

/**
 * Returns HTTP 401 Unauthorized when a request is unauthenticated.
 * Triggered by: missing/expired JWT, bad credentials.
 */
@Component
class RevPlayAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(RevPlayAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {

        log.warn("Unauthenticated access | path={} | reason={}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\"," +
                        "\"message\":\"Authentication is required to access this resource.\"," +
                        "\"path\":\"%s\"}",
                LocalDateTime.now(), request.getRequestURI()
        ));
    }
}

/**
 * Returns HTTP 403 Forbidden when an authenticated user lacks permission.
 * Triggered by: @PreAuthorize failures, role mismatches.
 */
@Component
class RevPlayAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(RevPlayAccessDeniedHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        log.warn("Access denied | path={} | reason={}", request.getRequestURI(), ex.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\"," +
                        "\"message\":\"You do not have permission to access this resource.\"," +
                        "\"path\":\"%s\"}",
                LocalDateTime.now(), request.getRequestURI()
        ));
    }
}

// ============================================================
// GLOBAL EXCEPTION HANDLER
// ============================================================

/**
 * Centralized exception handler for all RevPlay REST API controllers.
 *
 * All responses use {@link ErrorResponse} for consistent structure:
 *   - timestamp  : when the error occurred
 *   - status     : HTTP status code
 *   - error      : short HTTP status description
 *   - message    : human-readable detail
 *   - path       : request URI
 *   - fieldErrors: (validation only) per-field errors with rejectedValue
 *
 * Logging standard:
 *   WARN  — all 4xx client errors
 *   ERROR — 5xx unhandled server errors (with full stack trace)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 404 Not Found ─────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        log.warn("Resource not found: {} | path={}", ex.getMessage(), request.getRequestURI());

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

    // ── 400 Bad Request — @Valid Failures ─────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult bindingResult = ex.getBindingResult();

        List<ErrorResponse.FieldError> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : null,
                        fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value"
                ))
                .collect(Collectors.toList());

        log.warn("Validation failed: {} error(s) | path={}", fieldErrors.size(), request.getRequestURI());

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

        log.warn("Missing parameter: {} | path={}", ex.getParameterName(), request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), "Bad Request",
                String.format("Required parameter '%s' is missing.", ex.getParameterName()),
                request.getRequestURI()
        ));
    }

    // ── 400 Bad Request — Path Variable Type Mismatch ─────────────

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

    // ── 413 Payload Too Large ──────────────────────────────────────

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