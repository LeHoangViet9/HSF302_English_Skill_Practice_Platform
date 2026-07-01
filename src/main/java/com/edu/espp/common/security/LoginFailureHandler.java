package com.edu.espp.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maps authentication failures to a generic error code so /login never reveals
 * whether an account exists (BR-01-05). See UC-01-login.md §3.3, §3.5.
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        String errorCode = "INVALID_CREDENTIALS";

        if (exception instanceof DisabledException) {
            String message = exception.getMessage() == null ? "" : exception.getMessage();
            if (message.startsWith("EMAIL_NOT_VERIFIED")) {
                errorCode = "EMAIL_NOT_VERIFIED";
            } else if (message.startsWith("ACCOUNT_SUSPENDED")) {
                errorCode = "ACCOUNT_SUSPENDED";
            }
        }

        response.sendRedirect(request.getContextPath() + "/login?error=" + errorCode);
    }
}
