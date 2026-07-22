package com.edu.espp.service.auth;

import com.edu.espp.common.enums.UserStatus;
import com.edu.espp.entity.User;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StudentDetailsService
                implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String email)
                        throws UsernameNotFoundException {

                String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

                User user = userRepository
                                .findByEmail(normalizedEmail)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Email hoặc mật khẩu không đúng"));

                if (user.getStatus() != UserStatus.ACTIVE) {
                        throw new DisabledException(
                                        "Tài khoản không hoạt động");
                }

                return org.springframework.security.core.userdetails.User
                                .builder()
                                .username(user.getEmail())
                                .password(user.getPasswordHash())
                                .authorities("ROLE_" + user.getRole().name())
                                .build();
        }
}