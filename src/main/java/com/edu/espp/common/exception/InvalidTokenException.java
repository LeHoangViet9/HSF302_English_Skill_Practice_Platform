package com.edu.espp.common.exception;

/**
 * Thrown when an email_verification (or other auth_tokens) token is missing,
 * expired, or already used/revoked.
 * See .sdd/Spect/Backend/feat-auth/UC-02-register.md §3.2, §3.6, FR-DM-23.
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
