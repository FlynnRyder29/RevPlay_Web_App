package com.revplay.exception;

/**
 * Thrown when a request violates a business rule not covered by @Valid.
 * Maps to HTTP 400 Bad Request.
 *
 * Usage: throw new BadRequestException("Cannot delete an album that still has songs.");
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}