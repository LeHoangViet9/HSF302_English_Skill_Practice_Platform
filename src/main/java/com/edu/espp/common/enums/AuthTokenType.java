package com.edu.espp.common.enums;

/**
 * Type of an {@code auth_tokens} record.
 * Persisted to the {@code token_type} column as lowercase text via
 * {@link com.edu.espp.common.converter.AuthTokenTypeConverter}.
 */
public enum AuthTokenType {
    SESSION,
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
