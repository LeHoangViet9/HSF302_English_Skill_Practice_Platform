package edu.fu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async so EmailService.sendVerificationEmail(...) does not block the
 * register()/resendVerificationEmail() request thread (UC-02-register.md §3.1 Bước 8).
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
