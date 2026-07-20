package com.edu.espp.service.auth;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.common.exception.InvalidTokenException;
import com.edu.espp.common.exception.SamePasswordException;
import com.edu.espp.dto.ResetPasswordForm;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.repository.auth.AuthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private static final String RESET_TOKEN_INVALID_MESSAGE = "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn. "
            + "Vui lòng yêu cầu liên kết mới.";

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isValidResetToken(String tokenValue) {

        if (tokenValue == null || tokenValue.isBlank()) {
            return false;
        }

        Optional<AuthToken> optionalToken = authTokenRepository
                .findByTokenValueAndTokenType(
                        tokenValue,
                        AuthTokenType.PASSWORD_RESET);

        if (optionalToken.isEmpty()) {
            return false;
        }

        return isTokenActive(optionalToken.get());
    }

    @Transactional
    public ResetPasswordResult resetPassword(
            ResetPasswordForm form) {

        try {
            changePassword(
                    form.getToken(),
                    form.getNewPassword());

            return ResetPasswordResult.ok();

        } catch (InvalidTokenException exception) {

            return ResetPasswordResult.invalidToken(
                    exception.getMessage());

        } catch (SamePasswordException exception) {

            return ResetPasswordResult.error(
                    exception.getMessage());
        }
    }

    private void changePassword(
            String tokenValue,
            String newPassword) {

        AuthToken token = findValidToken(tokenValue);
        User user = token.getUser();

        if (user == null) {
            throw new InvalidTokenException(
                    RESET_TOKEN_INVALID_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        String currentPasswordHash = user.getPasswordHash();

        if (currentPasswordHash != null
                && !currentPasswordHash.isBlank()) {

            boolean samePassword = passwordEncoder.matches(
                    newPassword,
                    currentPasswordHash);

            if (samePassword) {
                throw new SamePasswordException();
            }
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPasswordHash(encodedPassword);

        userRepository.save(user);

        token.setUsedAt(now);
        authTokenRepository.save(token);

        revokeActiveTokens(
                user.getId(),
                AuthTokenType.SESSION,
                now);

        revokeActiveTokens(
                user.getId(),
                AuthTokenType.PASSWORD_RESET,
                now);

        log.info(
                "[ChangePasswordService] "
                        + "PASSWORD_RESET_SUCCESS {user_id={}}",
                user.getId());
    }

    private AuthToken findValidToken(String tokenValue) {

        if (tokenValue == null || tokenValue.isBlank()) {
            throw new InvalidTokenException(
                    RESET_TOKEN_INVALID_MESSAGE);
        }

        Optional<AuthToken> optionalToken = authTokenRepository
                .findByTokenValueAndTokenType(
                        tokenValue,
                        AuthTokenType.PASSWORD_RESET);

        if (optionalToken.isEmpty()) {
            throw new InvalidTokenException(
                    RESET_TOKEN_INVALID_MESSAGE);
        }

        AuthToken token = optionalToken.get();

        if (!isTokenActive(token)) {
            throw new InvalidTokenException(
                    RESET_TOKEN_INVALID_MESSAGE);
        }

        return token;
    }

    private boolean isTokenActive(AuthToken token) {

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        if (token.getRevokedAt() != null) {
            return false;
        }

        if (token.getUsedAt() != null) {
            return false;
        }

        return token.getExpiresAt() != null
                && token.getExpiresAt().isAfter(now);
    }

    private void revokeActiveTokens(
            Long userId,
            AuthTokenType tokenType,
            LocalDateTime now) {

        List<AuthToken> activeTokens = authTokenRepository
                .findByUser_IdAndTokenTypeAndRevokedAtIsNull(
                        userId,
                        tokenType);

        activeTokens.forEach(
                token -> token.setRevokedAt(now));

        authTokenRepository.saveAll(activeTokens);
    }

    public record ResetPasswordResult(
            boolean success,
            boolean invalidToken,
            String errorMessage) {

        static ResetPasswordResult ok() {

            return new ResetPasswordResult(
                    true,
                    false,
                    null);
        }

        static ResetPasswordResult invalidToken(
                String errorMessage) {

            return new ResetPasswordResult(
                    false,
                    true,
                    errorMessage);
        }

        static ResetPasswordResult error(
                String errorMessage) {

            return new ResetPasswordResult(
                    false,
                    false,
                    errorMessage);
        }
    }
}