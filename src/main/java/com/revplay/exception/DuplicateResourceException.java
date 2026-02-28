package com.revplay.exception;

/**
 * Thrown when a create/update operation violates a uniqueness rule.
 * Maps to HTTP 409 Conflict.
 *
 * Usage: throw new DuplicateResourceException("Email already registered: alice@mail.com");
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}