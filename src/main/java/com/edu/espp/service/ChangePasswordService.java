package com.edu.espp.service;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.common.exception.InvalidTokenException;
import com.edu.espp.common.exception.SamePasswordException;
import com.edu.espp.dto.ResetPasswordForm;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.repository.AuthTokenRepository;
import com.edu.espp.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private static final String RESET_TOKEN_INVALID_MESSAGE =
            "Link dat lai mat khau khong hop le hoac da het han. Vui long yeu cau lien ket moi.";

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isValidResetToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return false;
        }
        return authTokenRepository.findByTokenValueAndTokenType(tokenValue, AuthTokenType.PASSWORD_RESET)
                .filter(this::isTokenActive)
                .isPresent();
    }

    @Transactional
    public ResetPasswordResult resetPassword(ResetPasswordForm form) {
        try {
            resetPassword(form.getToken(), form.getNewPassword());
            return ResetPasswordResult.ok();
        } catch (InvalidTokenException ex) {
            return ResetPasswordResult.invalidToken(ex.getMessage());
        } catch (SamePasswordException ex) {
            return ResetPasswordResult.error(ex.getMessage());
        }
    }

    private void resetPassword(String tokenValue, String newPassword) {
        AuthToken token = findActiveResetToken(tokenValue);
        StudentUser student = token.getStudentUser();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        ensurePasswordIsNew(student, newPassword);
        updatePassword(student, newPassword, now);
        markTokenUsed(token, now);

        revokeAllActiveTokens(student.getStudentId(), AuthTokenType.SESSION, now);
        revokeAllActiveTokens(student.getStudentId(), AuthTokenType.PASSWORD_RESET, now);

        log.info("[ChangePasswordService] PASSWORD_RESET_SUCCESS {student_id={}}", student.getStudentId());
    }

    private AuthToken findActiveResetToken(String tokenValue) {
        AuthToken token = authTokenRepository
                .findByTokenValueAndTokenType(tokenValue, AuthTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidTokenException(RESET_TOKEN_INVALID_MESSAGE));

        if (!isTokenActive(token)) {
            throw new InvalidTokenException(RESET_TOKEN_INVALID_MESSAGE);
        }
        return token;
    }

    private void ensurePasswordIsNew(StudentUser student, String newPassword) {
        if (student.getPasswordHash() != null && passwordEncoder.matches(newPassword, student.getPasswordHash())) {
            throw new SamePasswordException();
        }
    }

    private void updatePassword(StudentUser student, String newPassword, LocalDateTime now) {
        student.setPasswordHash(passwordEncoder.encode(newPassword));
        student.setPasswordChangedAt(now);
        studentUserRepository.save(student);
    }

    private void markTokenUsed(AuthToken token, LocalDateTime now) {
        token.setUsedAt(now);
        authTokenRepository.save(token);
    }

    private boolean isTokenActive(AuthToken token) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return token.getRevokedAt() == null
                && token.getUsedAt() == null
                && token.getExpiresAt().isAfter(now);
    }

    private void revokeAllActiveTokens(Long studentId, AuthTokenType tokenType, LocalDateTime now) {
        List<AuthToken> tokens = authTokenRepository
                .findByStudentUser_StudentIdAndTokenTypeAndRevokedAtIsNull(studentId, tokenType);
        tokens.forEach(activeToken -> activeToken.setRevokedAt(now));
        authTokenRepository.saveAll(tokens);
    }

    public record ResetPasswordResult(boolean success, boolean invalidToken, String errorMessage) {

        static ResetPasswordResult ok() {
            return new ResetPasswordResult(true, false, null);
        }

        static ResetPasswordResult invalidToken(String errorMessage) {
            return new ResetPasswordResult(false, true, errorMessage);
        }

        static ResetPasswordResult error(String errorMessage) {
            return new ResetPasswordResult(false, false, errorMessage);
        }
    }
}
