package edu.fu.common.enums;

/**
 * Type of an {@code auth_tokens} record.
 * Persisted to the {@code token_type} column as lowercase text via
 * {@link edu.fu.common.converter.AuthTokenTypeConverter}.
 */
public enum AuthTokenType {
    SESSION,
    EMAIL_VERIFICATION,
    PASSWORD_RESET
}
