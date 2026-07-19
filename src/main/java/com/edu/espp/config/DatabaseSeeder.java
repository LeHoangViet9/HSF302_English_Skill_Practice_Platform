package com.edu.espp.config;

import com.edu.espp.common.enums.Role;
import com.edu.espp.common.enums.UserStatus;
import com.edu.espp.entity.StudentUser;
import com.edu.espp.entity.User;
import com.edu.espp.repository.StudentUserRepository;
import com.edu.espp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

        private static final String DEFAULT_PASSWORD = "123456";

        private final UserRepository userRepository;
        private final StudentUserRepository studentUserRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        @Transactional
        public void run(String... args) {

                seedUser(
                                "admin@espp.com",
                                "Admin ESPP",
                                Role.ADMIN);

                seedUser(
                                "staff@espp.com",
                                "Staff ESPP",
                                Role.STAFF);

                User studentUser = seedUser(
                                "student@espp.com",
                                "Student ESPP",
                                Role.STUDENT);

                seedStudentProfile(studentUser);

                log.info("DatabaseSeeder check completed");
        }

        private User seedUser(
                        String email,
                        String fullName,
                        Role role) {

                return userRepository
                                .findByEmail(email)
                                .map(existingUser -> {

                                        boolean changed = false;

                                        if (existingUser.getRole() != role) {
                                                existingUser.setRole(role);
                                                changed = true;
                                        }

                                        if (existingUser.getStatus() != UserStatus.ACTIVE) {

                                                existingUser.setStatus(
                                                                UserStatus.ACTIVE);

                                                changed = true;
                                        }

                                        if (changed) {
                                                return userRepository.save(
                                                                existingUser);
                                        }

                                        return existingUser;
                                })
                                .orElseGet(() -> {

                                        User user = User.builder()
                                                        .email(email)
                                                        .fullName(fullName)
                                                        .passwordHash(
                                                                        passwordEncoder.encode(
                                                                                        DEFAULT_PASSWORD))
                                                        .role(role)
                                                        .status(UserStatus.ACTIVE)
                                                        .loginAttempts(0)
                                                        .build();

                                        User savedUser = userRepository.save(user);

                                        log.info(
                                                        "Seeded user: {}",
                                                        email);

                                        return savedUser;
                                });
        }

        private void seedStudentProfile(User user) {

                if (studentUserRepository
                                .existsByUser_Id(user.getId())) {

                        return;
                }

                StudentUser studentProfile = StudentUser.builder()
                                .user(user)
                                .build();

                studentUserRepository.save(studentProfile);

                log.info(
                                "Seeded student profile for: {}",
                                user.getEmail());
        }
}