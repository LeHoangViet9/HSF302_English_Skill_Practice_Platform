package com.edu.espp.common.enums;

/**
 * Account lifecycle status of a {@code student_users} record.
 * Persisted to the {@code status} column as lowercase text via
 * {@link com.edu.espp.common.converter.StudentStatusConverter}.
 */
public enum StudentStatus {
    ACTIVE,
    SUSPENDED,
    DELETED
}
