package com.edu.espp.service;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.common.enums.StudentStatus;
import com.edu.espp.common.exception.InvalidTokenException;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.repository.auth.AuthTokenRepository;
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
public class VerifyEmailService {

    private static final int EMAIL_VERIFICATION_VALIDITY_HOURS = 24;
    private static final int RESEND_RATE_LIMIT_MAX = 3;
    private static final int RESEND_RATE_LIMIT_WINDOW_MINUTES = 60;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;
    private final com.edu.espp.service.auth.EmailService emailService;
    private final com.edu.espp.repository.UserRepository userRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public VerifyEmailResult verifyEmailForPage(String tokenValue) {
        try {
            verifyEmail(tokenValue);
            return VerifyEmailResult.ok();
        } catch (InvalidTokenException ex) {
            return VerifyEmailResult.invalid(ex.getMessage());
        }
    }

    @Transactional
    public void verifyEmail(String tokenValue) {
        AuthToken token = findEmailVerificationToken(tokenValue);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (!isTokenActive(token, now)) {
            throw new InvalidTokenException(
                    "Link khong hop le hoac da het han. Vui long yeu cau gui lai email xac minh.");
        }

        StudentUser student = studentUserRepository.findByEmail(token.getUser().getEmail())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        activateStudent(student, now);
        markTokenUsed(token, now);

        log.info("[VerifyEmailService] EMAIL_VERIFIED {student_id={}}", student.getStudentId());
    }

    @Transactional
    public ResendResult resendVerificationEmail(String email) {
        Optional<StudentUser> maybeStudent = studentUserRepository.findByEmail(normalizeEmail(email));

        if (maybeStudent.isEmpty() || maybeStudent.get().getStatus() != StudentStatus.PENDING) {
            return ResendResult.ok();
        }

        StudentUser student = maybeStudent.get();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        ResendResult rateLimit = checkResendRateLimit(student, now);
        if (rateLimit.rateLimited()) {
            return rateLimit;
        }

        revokeActiveVerificationTokens(student, now);
        issueEmailVerificationToken(student);

        return ResendResult.ok();
    }

    void issueEmailVerificationToken(StudentUser student) {
        String tokenValue = generateSecureToken();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        com.edu.espp.entity.User user = userRepository.findByEmail(student.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        AuthToken token = AuthToken.builder()
                .user(user)
                .tokenType(AuthTokenType.EMAIL_VERIFICATION)
                .tokenValue(tokenValue)
                .expiresAt(now.plusHours(EMAIL_VERIFICATION_VALIDITY_HOURS))
                .build();

        authTokenRepository.save(token);

        String verifyLink = baseUrl + "/verify-email?token=" + tokenValue;
        emailService.sendVerificationEmail(student.getEmail(), verifyLink);
    }

    private AuthToken findEmailVerificationToken(String tokenValue) {
        return authTokenRepository
                .findByTokenValueAndTokenType(tokenValue, AuthTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new InvalidTokenException("Link khong hop le"));
    }

    private boolean isTokenActive(AuthToken token, LocalDateTime now) {
        return token.getRevokedAt() == null
                && token.getUsedAt() == null
                && token.getExpiresAt().isAfter(now);
    }

    private void activateStudent(StudentUser student, LocalDateTime now) {
        student.setStatus(StudentStatus.ACTIVE);
        student.setEmailVerifiedAt(now);
        studentUserRepository.save(student);
    }

    private void markTokenUsed(AuthToken token, LocalDateTime now) {
        token.setUsedAt(now);
        token.setRevokedAt(now);
        authTokenRepository.save(token);
    }

    private ResendResult checkResendRateLimit(StudentUser student, LocalDateTime now) {
        LocalDateTime windowStart = now.minusMinutes(RESEND_RATE_LIMIT_WINDOW_MINUTES);
        com.edu.espp.entity.User user = userRepository.findByEmail(student.getEmail()).orElseThrow();
        List<AuthToken> recentTokens = authTokenRepository
                .findByUser_IdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc(
                        user.getId(), AuthTokenType.EMAIL_VERIFICATION, windowStart);

        if (recentTokens.size() < RESEND_RATE_LIMIT_MAX) {
            return ResendResult.ok();
        }

        return ResendResult.rateLimited(minutesUntilRetry(now, recentTokens.get(0).getCreatedAt()));
    }

    private long minutesUntilRetry(LocalDateTime now, LocalDateTime earliestCreatedAt) {
        long minutes = Duration.between(now, earliestCreatedAt.plusMinutes(RESEND_RATE_LIMIT_WINDOW_MINUTES))
                .toMinutes() + 1;
        return Math.max(minutes, 1);
    }

    private void revokeActiveVerificationTokens(StudentUser student, LocalDateTime now) {
        com.edu.espp.entity.User user = userRepository.findByEmail(student.getEmail()).orElseThrow();
        List<AuthToken> activeTokens = authTokenRepository
                .findByUser_IdAndTokenTypeAndRevokedAtIsNull(
                        user.getId(), AuthTokenType.EMAIL_VERIFICATION);
        activeTokens.forEach(activeToken -> activeToken.setRevokedAt(now));
        authTokenRepository.saveAll(activeTokens);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record ResendResult(boolean rateLimited, long retryAfterMinutes) {

        static ResendResult ok() {
            return new ResendResult(false, 0);
        }

        static ResendResult rateLimited(long minutes) {
            return new ResendResult(true, minutes);
        }
    }

    public record VerifyEmailResult(boolean success, String errorMessage) {

        static VerifyEmailResult ok() {
            return new VerifyEmailResult(true, null);
        }

        static VerifyEmailResult invalid(String errorMessage) {
            return new VerifyEmailResult(false, errorMessage);
        }
    }
}
