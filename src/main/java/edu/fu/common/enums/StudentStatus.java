package edu.fu.common.enums;

/**
 * Account lifecycle status of a {@code student_users} record.
 * Persisted to the {@code status} column as lowercase text via
 * {@link edu.fu.common.converter.StudentStatusConverter}.
 */
public enum StudentStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    DELETED
}
