package com.edu.espp.config;

import com.edu.espp.common.enums.Role;
import com.edu.espp.common.enums.StudentStatus;
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

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final StudentUserRepository studentUserRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("DatabaseSeeder checking and seeding test accounts...");

        String passwordHash = passwordEncoder.encode("123456");

        // 1. Seed Admin account: admin@espp.com
        String adminEmail = "admin@espp.com";
        if (!studentUserRepository.existsByEmail(adminEmail)) {
            StudentUser adminStudent = StudentUser.builder()
                    .email(adminEmail)
                    .passwordHash(passwordHash)
                    .fullName("Admin ESPP")
                    .status(StudentStatus.ACTIVE)
                    .isDeleted(false)
                    .loginAttempts(0)
                    .build();
            studentUserRepository.save(adminStudent);
            log.info("Seeded Admin student_user: {}", adminEmail);
        }

        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordHash)
                    .fullName("Admin ESPP")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(adminUser);
            log.info("Seeded Admin user: {}", adminEmail);
        }

        // 2. Seed Student account: student@espp.com
        String studentEmail = "student@espp.com";
        if (!studentUserRepository.existsByEmail(studentEmail)) {
            StudentUser studentStudent = StudentUser.builder()
                    .email(studentEmail)
                    .passwordHash(passwordHash)
                    .fullName("Student ESPP")
                    .status(StudentStatus.ACTIVE)
                    .isDeleted(false)
                    .loginAttempts(0)
                    .build();
            studentUserRepository.save(studentStudent);
            log.info("Seeded Student student_user: {}", studentEmail);
        }

        if (!userRepository.findByEmail(studentEmail).isPresent()) {
            User studentUser = User.builder()
                    .email(studentEmail)
                    .passwordHash(passwordHash)
                    .fullName("Student ESPP")
                    .role(Role.STUDENT)
                    .status(UserStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(studentUser);
            log.info("Seeded Student user: {}", studentEmail);
        }

        log.info("DatabaseSeeder check completed.");
    }
}
