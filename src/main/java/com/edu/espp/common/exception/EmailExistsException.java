package com.edu.espp.common.exception;

/**
 * Thrown when a registration email already exists in student_users.
 * See .sdd/Spect/Backend/feat-auth/UC-02-register.md §3.4, FR-AUTH-11.
 */
public class EmailExistsException extends RuntimeException {

    public EmailExistsException() {
        super("Email đã được sử dụng");
    }
}
