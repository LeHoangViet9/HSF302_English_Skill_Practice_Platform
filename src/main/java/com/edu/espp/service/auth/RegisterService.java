package com.edu.espp.service.auth;

import com.edu.espp.common.enums.Role;
import com.edu.espp.common.enums.UserStatus;
import com.edu.espp.common.exception.EmailExistsException;
import com.edu.espp.dto.RegisterForm;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.entity.User;
import com.edu.espp.repository.StudentUserRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegisterService {

        private final UserRepository userRepository;
        private final StudentUserRepository studentUserRepository;
        private final PasswordEncoder passwordEncoder;

        @Transactional
        public String register(RegisterForm form) {

                String email = form.getEmail()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                String fullName = form.getFullName().trim();

                if (userRepository.existsByEmail(email)) {
                        throw new EmailExistsException();
                }

                User user = User.builder()
                                .email(email)
                                .fullName(fullName)
                                .passwordHash(
                                                passwordEncoder.encode(
                                                                form.getPassword()))
                                .role(Role.STUDENT)
                                .status(UserStatus.ACTIVE)
                                .build();

                userRepository.save(user);

                StudentUser student = StudentUser.builder()
                                .email(email)
                                .fullName(fullName)
                                .passwordHash(user.getPasswordHash())
                                .build();

                studentUserRepository.save(student);

                return email;
        }
}