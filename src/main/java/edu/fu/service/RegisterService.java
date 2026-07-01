package edu.fu.service;

import edu.fu.common.enums.StudentStatus;
import edu.fu.common.exception.EmailExistsException;
import edu.fu.dto.RegisterForm;
import edu.fu.entity.StudentUser;
import edu.fu.repository.StudentUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final StudentUserRepository studentUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerifyEmailService verifyEmailService;

    @Transactional
    public String register(RegisterForm form) {
        String normalizedEmail = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String fullName = form.getFullName().trim();

        if (studentUserRepository.existsByEmail(normalizedEmail)) {
            throw new EmailExistsException();
        }

        StudentUser student = StudentUser.builder()
                .email(normalizedEmail)
                .fullName(fullName)
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .status(StudentStatus.PENDING)
                .loginAttempts(0)
                .isDeleted(false)
                .build();

        try {
            student = studentUserRepository.save(student);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailExistsException();
        }

        verifyEmailService.issueEmailVerificationToken(student);

        log.info("[RegisterService] REGISTER_SUCCESS {email={}}", normalizedEmail);
        return normalizedEmail;
    }
}
