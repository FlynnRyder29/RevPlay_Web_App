package com.revplay.exception;

/**
 * Thrown when a user attempts an action they are not permitted to perform.
 * Maps to HTTP 403 Forbidden.
 *
 * Usage: throw new UnauthorizedAccessException("You cannot delete this playlist.");
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}