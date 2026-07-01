package edu.fu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Split out from SecurityConfig: SecurityConfig depends on LoginSuccessHandler,
 * and register/reset-password services depend on PasswordEncoder. Keeping the
 * bean here avoids circular references with SecurityConfig.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
