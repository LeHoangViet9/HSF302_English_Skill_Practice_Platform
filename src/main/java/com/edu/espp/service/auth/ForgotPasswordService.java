package com.edu.espp.service.auth;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.common.enums.UserStatus;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.repository.auth.AuthTokenRepository;
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
        private static final int PASSWORD_RESET_RATE_LIMIT_WINDOW_SECONDS = 30;
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        private final UserRepository userRepository;
        private final AuthTokenRepository authTokenRepository;
        private final EmailService emailService;

        @Value("${app.base-url}")
        private String baseUrl;

        @Transactional
        public PasswordResetRequestResult requestPasswordReset(
                        String email) {

                Optional<User> optionalUser = userRepository.findByEmail(
                                normalizeEmail(email));

                if (optionalUser.isEmpty()) {
                        return PasswordResetRequestResult.ok();
                }

                User user = optionalUser.get();

                if (user.getPasswordHash() == null
                                || user.getPasswordHash().isBlank()
                                || user.getStatus() != UserStatus.ACTIVE) {

                        return PasswordResetRequestResult.ok();
                }

                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

                PasswordResetRequestResult rateLimitResult = checkResetRequestRateLimit(user, now);

                if (rateLimitResult.rateLimited()) {
                        return rateLimitResult;
                }

                revokeActiveResetTokens(user, now);
                sendResetPasswordToken(user, now);

                log.info(
                                "[ForgotPasswordService] "
                                                + "PASSWORD_RESET_REQUESTED {user_id={}}",
                                user.getId());

                return PasswordResetRequestResult.ok();
        }

        private PasswordResetRequestResult checkResetRequestRateLimit(
                        User user,
                        LocalDateTime now) {

                LocalDateTime windowStart = now.minusSeconds(
                                PASSWORD_RESET_RATE_LIMIT_WINDOW_SECONDS);

                List<AuthToken> recentTokens = authTokenRepository
                                .findByUser_IdAndTokenTypeAndCreatedAtAfterOrderByCreatedAtAsc(
                                                user.getId(),
                                                AuthTokenType.PASSWORD_RESET,
                                                windowStart);

                if (recentTokens.size() < PASSWORD_RESET_RATE_LIMIT_MAX) {

                        return PasswordResetRequestResult.ok();
                }

                long retryAfterSeconds = secondsUntilRetry(
                                now,
                                recentTokens.get(0).getCreatedAt());

                return PasswordResetRequestResult.rateLimited(
                                retryAfterSeconds);
        }

        private long secondsUntilRetry(
                        LocalDateTime now,
                        LocalDateTime earliestCreatedAt) {

                LocalDateTime retryAt = earliestCreatedAt.plusSeconds(
                                PASSWORD_RESET_RATE_LIMIT_WINDOW_SECONDS);

                long seconds = Duration
                                .between(now, retryAt)
                                .toSeconds() + 1;

                return Math.max(seconds, 1);
        }

        private void revokeActiveResetTokens(
                        User user,
                        LocalDateTime now) {

                List<AuthToken> activeResetTokens = authTokenRepository
                                .findByUser_IdAndTokenTypeAndRevokedAtIsNull(
                                                user.getId(),
                                                AuthTokenType.PASSWORD_RESET);

                activeResetTokens.forEach(
                                token -> token.setRevokedAt(now));

                authTokenRepository.saveAll(activeResetTokens);
        }

        private void sendResetPasswordToken(
                        User user,
                        LocalDateTime now) {

                String tokenValue = generateSecureToken();

                AuthToken token = AuthToken.builder()
                                .user(user)
                                .tokenType(AuthTokenType.PASSWORD_RESET)
                                .tokenValue(tokenValue)
                                .expiresAt(
                                                now.plusMinutes(
                                                                PASSWORD_RESET_VALIDITY_MINUTES))
                                .build();

                authTokenRepository.save(token);

                String resetLink = baseUrl
                                + "/reset-password?token="
                                + tokenValue;

                emailService.sendResetPasswordEmail(
                                user.getEmail(),
                                resetLink);
        }

        private String normalizeEmail(String email) {

                return email == null
                                ? ""
                                : email.trim().toLowerCase(Locale.ROOT);
        }

        private String generateSecureToken() {

                byte[] bytes = new byte[32];

                SECURE_RANDOM.nextBytes(bytes);

                return Base64
                                .getUrlEncoder()
                                .withoutPadding()
                                .encodeToString(bytes);
        }

        public record PasswordResetRequestResult(
                        boolean rateLimited,
                        long retryAfterSeconds) {

                static PasswordResetRequestResult ok() {

                        return new PasswordResetRequestResult(
                                        false,
                                        0);
                }

                static PasswordResetRequestResult rateLimited(
                                long seconds) {

                        return new PasswordResetRequestResult(
                                        true,
                                        seconds);
                }
        }
}