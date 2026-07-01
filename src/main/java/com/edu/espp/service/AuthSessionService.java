package com.edu.espp.service;

import com.edu.espp.common.enums.AuthTokenType;
import com.edu.espp.entity.AuthToken;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.repository.AuthTokenRepository;
import com.edu.espp.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private static final int SESSION_TOKEN_VALIDITY_DAYS = 7;

    private final StudentUserRepository studentUserRepository;
    private final AuthTokenRepository authTokenRepository;

    @Transactional
    public void handleLoginSuccess(String email, String sessionId, String ipAddress) {
        StudentUser student = studentUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Student not found for authenticated email: " + email));

        AuthToken sessionToken = AuthToken.builder()
                .studentUser(student)
                .tokenType(AuthTokenType.SESSION)
                .tokenValue(sessionId)
                .expiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(SESSION_TOKEN_VALIDITY_DAYS))
                .ipAddress(ipAddress)
                .build();

        authTokenRepository.save(sessionToken);
    }

    @Transactional
    public void revokeCurrentSessionToken(String sessionId, Authentication authentication, String ipAddress) {
        if (sessionId == null) {
            log.warn("[AuthSessionService] No auth_token found for session - proceeding {session_id=null}");
            return;
        }

        authTokenRepository.findByTokenValueAndTokenTypeAndRevokedAtIsNull(sessionId, AuthTokenType.SESSION)
                .ifPresentOrElse(
                        token -> {
                            token.setRevokedAt(LocalDateTime.now(ZoneOffset.UTC));
                            authTokenRepository.save(token);

                            Long studentId = resolveStudentId(token, authentication);
                            log.info("[AuthSessionService] LOGOUT_SUCCESS {student_id={}, ip={}, session_id={}}",
                                    studentId, ipAddress, sessionId);
                        },
                        () -> log.warn("[AuthSessionService] No auth_token found for session - proceeding {session_id={}}",
                                sessionId));
    }

    private Long resolveStudentId(AuthToken token, Authentication authentication) {
        if (token.getStudentUser() != null) {
            return token.getStudentUser().getStudentId();
        }
        if (authentication != null) {
            return studentUserRepository.findByEmail(authentication.getName())
                    .map(StudentUser::getStudentId)
                    .orElse(null);
        }
        return null;
    }
}
