package com.beautystock.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // =========================
    // ✅ WELCOME EMAIL
    // =========================
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            if (isEmailNotConfigured()) return;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("✨ Welcome to BeautyStock");
            message.setText(buildWelcomeEmailBody(fullName));

            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send welcome email: {}", e.getMessage());
        }
    }

    // =========================
    // ✅ LOGIN EMAIL
    // =========================
    public void sendLoginNotificationEmail(String toEmail, String fullName) {
        try {
            if (isEmailNotConfigured()) return;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("✨ Welcome back to BeautyStock");
            message.setText(buildLoginBody(fullName));

            mailSender.send(message);
            log.info("Login email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send login email: {}", e.getMessage());
        }
    }

    // =========================
    // ✅ EXPIRATION EMAIL
    // =========================
    public void sendExpirationReminder(String toEmail, String productName, String expirationDate, int days) {
        try {
            if (isEmailNotConfigured()) return;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);

            message.setSubject("⏰ " + productName + " expires in " + days + " day(s)");

            message.setText(buildExpirationBody(productName, expirationDate, days));

            mailSender.send(message);

            log.info("Expiration email sent to {} for {}", toEmail, productName);

        } catch (Exception e) {
            log.error("Failed to send expiration email: {}", e.getMessage());
        }
    }

    // =========================
    // ✅ CUSTOM EMAIL (FIXES YOUR ERROR)
    // =========================
    public void sendCustomEmail(String toEmail, String subject, String body) {
        try {
            if (isEmailNotConfigured()) return;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Custom email sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send custom email: {}", e.getMessage());
        }
    }

    // =========================
    // ✨ MESSAGE BUILDERS
    // =========================

    private String buildWelcomeEmailBody(String name) {
        return "Dearest " + name + ",\n\n" +
               "Welcome to BeautyStock ✨\n\n" +
               "Your beauty collection now has a refined and elegant home.\n\n" +
               "Yours sincerely,\n" +
               "— BeautyStock Society";
    }

    private String buildLoginBody(String name) {
        return "Welcome back, " + name + " ✨\n\n" +
               "Your collection awaits your graceful touch.\n\n" +
               "— BeautyStock";
    }

    private String buildExpirationBody(String productName, String date, int days) {
        return String.format(
            "My Dearest Beauty Enthusiast,\n\n" +
            "Your treasured item '%s' shall expire in %d day(s).\n" +
            "Final date: %s.\n\n" +
            "Pray, attend to it with haste lest it lose its charm.\n\n" +
            "With elegance and care,\n" +
            "— BeautyStock Society ✨",
            productName, days, date
        );
    }

    // =========================
    // ⚠️ CONFIG CHECK
    // =========================
    private boolean isEmailNotConfigured() {
        if (fromEmail == null || fromEmail.isEmpty() || fromEmail.contains("${")) {
            log.warn("Email not configured. Skipping send.");
            return true;
        }
        return false;
    }
}