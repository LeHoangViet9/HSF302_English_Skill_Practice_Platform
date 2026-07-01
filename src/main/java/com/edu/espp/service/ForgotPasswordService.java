package com.edu.espp.service;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.repository.AuthTokenRepository;
import com.edu.espp.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private static final int PASSWORD_RESET_VALIDITY_MINUTES = 15;
    private static final int PASSWORD_RESET_RATE_LIMIT_MAX = 3;
    private static final int PASSWORD_RESET_RATE_LIMIT_WINDOW_MINUTES = 60;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public PasswordResetRequestResult requestPasswordReset(String email) {
        Optional<StudentUser> maybeStudent = studentUserRepository.findByEmailAndIsDeletedFalse(normalizeEmail(email));

        if (maybeStudent.isEmpty() || maybeStudent.get().getPasswordHash() == null) {
            return PasswordResetRequestResult.ok();
        }

        StudentUser student = maybeStudent.get();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        PasswordResetRequestResult rateLimit = checkResetRequestRateLimit(student, now);
        if (rateLimit.rateLimited()) {
            return rateLimit;
        }

        revokeActiveResetTokens(student, now);
        sendResetPasswordToken(student, now);

        log.info("[ForgotPasswordService] PASSWORD_RESET_REQUESTED {student_id={}}", student.getStudentId());
        return PasswordResetRequestResult.ok();
    }

    private PasswordResetRequestResult checkResetRequestRateLimit(StudentUser student, LocalDateTime now) {
        LocalDateTime windowStart = now.minusMinutes(PASSWORD_RESET_RATE_LIMIT_WINDOW_MINUTES);
        List<AuthToken> recentTokens = authTokenRepository
                .findByStudentUser_StudentIdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc(
                        student.getStudentId(), AuthTokenType.PASSWORD_RESET, windowStart);

        if (recentTokens.size() < PASSWORD_RESET_RATE_LIMIT_MAX) {
            return PasswordResetRequestResult.ok();
        }

        return PasswordResetRequestResult.rateLimited(minutesUntilRetry(now, recentTokens.get(0).getCreatedAt()));
    }

    private long minutesUntilRetry(LocalDateTime now, LocalDateTime earliestCreatedAt) {
        long minutes = Duration
                .between(now, earliestCreatedAt.plusMinutes(PASSWORD_RESET_RATE_LIMIT_WINDOW_MINUTES))
                .toMinutes() + 1;
        return Math.max(minutes, 1);
    }

    private void revokeActiveResetTokens(StudentUser student, LocalDateTime now) {
        List<AuthToken> activeResetTokens = authTokenRepository
                .findByStudentUser_StudentIdAndTokenTypeAndRevokedAtIsNull(
                        student.getStudentId(), AuthTokenType.PASSWORD_RESET);
        activeResetTokens.forEach(activeToken -> activeToken.setRevokedAt(now));
        authTokenRepository.saveAll(activeResetTokens);
    }

    private void sendResetPasswordToken(StudentUser student, LocalDateTime now) {
        String tokenValue = generateSecureToken();
        AuthToken token = AuthToken.builder()
                .studentUser(student)
                .tokenType(AuthTokenType.PASSWORD_RESET)
                .tokenValue(tokenValue)
                .expiresAt(now.plusMinutes(PASSWORD_RESET_VALIDITY_MINUTES))
                .build();
        authTokenRepository.save(token);

        String resetLink = baseUrl + "/reset-password?token=" + tokenValue;
        emailService.sendResetPasswordEmail(student.getEmail(), resetLink);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record PasswordResetRequestResult(boolean rateLimited, long retryAfterMinutes) {

        static PasswordResetRequestResult ok() {
            return new PasswordResetRequestResult(false, 0);
        }

        static PasswordResetRequestResult rateLimited(long minutes) {
            return new PasswordResetRequestResult(true, minutes);
        }
    }
}
