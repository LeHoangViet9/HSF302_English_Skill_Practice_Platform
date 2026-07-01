package edu.fu.service;

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

import java.util.Properties;

/**
 * Sends auth emails through a hand-configured jakarta.mail SMTP session
 * (PLAN.md §2.1 — no spring-boot-starter-mail auto-config). If MAIL_USERNAME/
 * MAIL_PASSWORD aren't set (e.g. local dev without a .env mail block), it logs
 * the link instead of throwing, so register()/resendVerificationEmail() never
 * fail because of email delivery.
 */
@Slf4j
@Service
public class EmailService {

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
    public void sendVerificationEmail(String email, String verifyLink) {
        String subject = "Xác minh tài khoản Apolo";
        String body = "Xin chào,\n\n"
                + "Nhấp vào liên kết sau để kích hoạt tài khoản (hết hạn sau 24 giờ):\n"
                + verifyLink
                + "\nNếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.";
        send(email, subject, body);
    }

    @Async
    public void sendResetPasswordEmail(String email, String resetLink) {
        String subject = "Đặt lại mật khẩu Apolo";
        String body = "Xin chào,\n\n"
                + "Nhấp vào liên kết sau để đặt lại mật khẩu (hết hạn sau 15 phút):\n"
                + resetLink
                + "\n\nNếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.";
        send(email, subject, body);
    }

    private void send(String to, String subject, String body) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn(
                    "[EmailService] MAIL_USERNAME/MAIL_PASSWORD chưa cấu hình, chỉ log link thay vì gửi thật: {} -> {}",
                    to, verifyLinkFromBody(body));
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            String fromAddress = (from == null || from.isBlank()) ? username : from;

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            Transport.send(message);
            log.info("[EmailService] Sent email to {}", to);
        } catch (MessagingException ex) {
            log.error("[EmailService] Failed to send email to {}", to, ex);
        }
    }

    private String verifyLinkFromBody(String body) {
        int idx = body.indexOf("http");
        return idx >= 0 ? body.substring(idx).split("\n")[0] : body;
    }
}
