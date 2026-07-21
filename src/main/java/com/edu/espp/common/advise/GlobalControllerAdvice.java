package com.edu.espp.common.advise;

import com.edu.espp.common.enums.Role;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import com.edu.espp.common.utils.UrlUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final UrlUtils urlUtils;

    @ModelAttribute("urlUtils")
    public UrlUtils urlUtils() {
        return urlUtils;
    }

    @ModelAttribute("user")
    public User populateUser(HttpSession session) {
        // 1. Try to get from Spring Security principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<User> maybeUser = userRepository.findByEmail(email);
            if (maybeUser.isPresent()) {
                session.setAttribute("currentUser", maybeUser.get());
                return maybeUser.get();
            }
        }

        // 2. Try to get from session
        User user = (User) session.getAttribute("currentUser");
        if (user != null) {
            return user;
        }

        // 3. Fallback mock user (so pages never crash)
        user = User.builder()
                .id(1L)
                .email("student@espp.com")
                .fullName("Học viên (Default)")
                .role(Role.STUDENT)
                .build();
        session.setAttribute("currentUser", user);
        return user;
    }
}
