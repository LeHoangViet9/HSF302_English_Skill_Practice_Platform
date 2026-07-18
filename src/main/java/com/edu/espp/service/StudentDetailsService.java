package com.edu.espp.service;

import com.edu.espp.entity.StudentUser;
import com.edu.espp.common.enums.StudentStatus;
import com.edu.espp.common.enums.UserStatus;
import com.edu.espp.common.enums.Role;
import com.edu.espp.repository.UserRepository;
import com.edu.espp.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads Student credentials for Spring Security form login.
 * See .sdd/Spect/Backend/feat-auth/UC-01-login.md §3.1 Bước 4-8.
 */
@Service
@RequiredArgsConstructor
public class StudentDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StudentUserRepository studentUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user in the central users table first
        com.edu.espp.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        // If the user is banned globally
        if (user.getStatus() == UserStatus.BANNED) {
            throw new DisabledException("ACCOUNT_SUSPENDED");
        }

        // If it's a student, perform extra status checks
        if (user.getRole() == Role.STUDENT) {
            StudentUser student = studentUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

            if (Boolean.TRUE.equals(student.getIsDeleted()) || student.getStatus() == StudentStatus.DELETED) {
                throw new UsernameNotFoundException("Invalid credentials");
            }

            if (student.getStatus() == StudentStatus.PENDING) {
                throw new DisabledException("EMAIL_NOT_VERIFIED");
            }

            if (student.getStatus() == StudentStatus.SUSPENDED) {
                String reason = student.getSuspendReason() == null ? "" : student.getSuspendReason();
                throw new DisabledException("ACCOUNT_SUSPENDED:" + reason);
            }
        }

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .build();
    }
}
