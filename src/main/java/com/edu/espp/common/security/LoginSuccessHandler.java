package com.edu.espp.common.security;

import com.edu.espp.service.AuthSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * See .sdd/Spect/Backend/feat-auth/UC-01-login.md §3.1 Bước 9-10.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthSessionService authSessionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        String email = authentication.getName();
        String sessionId = request.getSession().getId();
        String ipAddress = request.getRemoteAddr();

        authSessionService.handleLoginSuccess(email, sessionId, ipAddress);

        response.sendRedirect(request.getContextPath() + "/exams");
    }
}
