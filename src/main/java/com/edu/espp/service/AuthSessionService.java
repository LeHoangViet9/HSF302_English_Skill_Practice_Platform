package com.edu.espp.service;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.repository.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSessionService {

        private static final int SESSION_TOKEN_VALIDITY_DAYS = 7;

        private final UserRepository userRepository;
        private final AuthTokenRepository authTokenRepository;

        @Transactional
        public void handleLoginSuccess(
                        String email,
                        String sessionId,
                        String ipAddress) {

                String normalizedEmail = email
                                .trim()
                                .toLowerCase(Locale.ROOT);

                User user = userRepository
                                .findByEmail(normalizedEmail)
                                .orElseThrow(() -> new IllegalStateException(
                                                "User not found for authenticated email: "
                                                                + normalizedEmail));

                AuthToken sessionToken = new AuthToken();

                sessionToken.setUser(user);
                sessionToken.setTokenType(
                                AuthTokenType.SESSION);
                sessionToken.setTokenValue(sessionId);
                sessionToken.setIpAddress(ipAddress);
                sessionToken.setExpiresAt(
                                LocalDateTime.now(ZoneOffset.UTC)
                                                .plusDays(
                                                                SESSION_TOKEN_VALIDITY_DAYS));

                authTokenRepository.save(sessionToken);
        }

        @Transactional
        public void revokeCurrentSessionToken(
                        String sessionId,
                        Authentication authentication,
                        String ipAddress) {

                if (sessionId == null || sessionId.isBlank()) {

                        log.warn(
                                        "[AuthSessionService] "
                                                        + "No auth token found: session ID is empty");

                        return;
                }

                Optional<AuthToken> optionalToken = authTokenRepository
                                .findByTokenValueAndTokenTypeAndRevokedAtIsNull(
                                                sessionId,
                                                AuthTokenType.SESSION);

                if (optionalToken.isEmpty()) {

                        log.warn(
                                        "[AuthSessionService] "
                                                        + "No auth token found for session {}",
                                        sessionId);

                        return;
                }

                AuthToken token = optionalToken.get();

                token.setRevokedAt(
                                LocalDateTime.now(ZoneOffset.UTC));

                authTokenRepository.save(token);

                Long userId = getUserId(
                                token,
                                authentication);

                log.info(
                                "[AuthSessionService] LOGOUT_SUCCESS "
                                                + "{user_id={}, ip={}, session_id={}}",
                                userId,
                                ipAddress,
                                sessionId);
        }

        private Long getUserId(
                        AuthToken token,
                        Authentication authentication) {

                if (token.getUser() != null) {
                        return token.getUser().getId();
                }

                if (authentication == null
                                || authentication.getName() == null) {

                        return null;
                }

                String email = authentication
                                .getName()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                return userRepository
                                .findByEmail(email)
                                .map(User::getId)
                                .orElse(null);
        }
}
