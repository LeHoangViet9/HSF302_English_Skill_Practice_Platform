package com.edu.espp.service.auth;

import com.edu.espp.entity.StudentUser;
import com.edu.espp.common.enums.StudentStatus;
import com.edu.espp.repository.auth.StudentUserRepository;
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

    private final StudentUserRepository studentUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
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

        return User.builder()
                .username(student.getEmail())
                .password(student.getPasswordHash() == null ? "" : student.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_STUDENT")))
                .build();
    }
}
