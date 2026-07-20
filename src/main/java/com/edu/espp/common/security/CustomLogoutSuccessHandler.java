package com.edu.espp.common.security;

import com.edu.espp.service.auth.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * See .sdd/Spect/Backend/feat-auth/UC-18-logout.md §3.1 Bước 5-7.
 * Revokes the auth_tokens record for the current session, then redirects to
 * /login regardless of outcome — logout must never fail with a 500 (BR-18-05,
 * AC-18-02).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuthSessionService authSessionService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            try {
                String sessionId = session.getId();
                String ipAddress = resolveIpAddress(request);
                authSessionService.revokeCurrentSessionToken(sessionId, authentication, ipAddress);
            } catch (Exception ex) {
                log.error("[AuthSessionService] Error revoking session token during logout", ex);
            }
        }

        response.sendRedirect(request.getContextPath() + "/login?logout");
    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
