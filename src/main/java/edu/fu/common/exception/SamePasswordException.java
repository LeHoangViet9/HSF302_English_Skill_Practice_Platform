package edu.fu.common.exception;

/**
 * Thrown when a reset-password submission's new password matches the
 * account's current password.
 * See .sdd/Spect/Backend/feat-auth/UC-03-reset-password.md §3.2 Bước 10, BR-03-08.
 */
public class SamePasswordException extends RuntimeException {

    public SamePasswordException() {
        super("Mật khẩu mới không được giống mật khẩu cũ");
    }
}
