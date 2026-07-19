package com.edu.espp.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

@Slf4j
@Service
public class EmailService {

    private final TemplateEngine templateEngine;

    public EmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Value("${app.mail.host}")
    private String host;

    @Value("${app.mail.port}")
    private int port;

    @Value("${app.mail.username:}")
    private String username;

    @Value("${app.mail.password:}")
    private String password;

    @Value("${app.mail.from:}")
    private String from;

    @Async
    public void sendResetPasswordEmail(
            String email,
            String resetLink) {

        String subject = "Đặt lại mật khẩu Apolo";

        Context context = new Context();
        context.setVariable("resetLink", resetLink);

        String htmlBody = templateEngine.process(
                "reset-password",
                context);

        send(email, subject, htmlBody);
    }

    private void send(
            String to,
            String subject,
            String htmlBody) {

        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {

            log.warn("Gmail chưa được cấu hình");
            return;
        }

        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                username,
                                password);
                    }
                });

        try {
            String fromAddress = from == null || from.isBlank()
                    ? username
                    : from;

            MimeMessage message = new MimeMessage(session);

            message.setFrom(
                    new InternetAddress(fromAddress));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to));

            message.setSubject(
                    subject,
                    "UTF-8");

            message.setContent(
                    htmlBody,
                    "text/html; charset=UTF-8");

            Transport.send(message);

            log.info(
                    "[EmailService] Sent email to {}",
                    to);

        } catch (MessagingException exception) {

            log.error(
                    "[EmailService] Failed to send email to {}",
                    to,
                    exception);
        }
    }
}
