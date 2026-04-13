package com.beautystock.service;

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

    /**
     * Send a welcome email to a newly registered user
     */
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to BeautyStock!");
            message.setText(buildWelcomeEmailBody(fullName));
            
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildWelcomeEmailBody(String fullName) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to BeautyStock! 🌸\n\n" +
            "We're excited to have you join our community. BeautyStock is your personal beauty collection tracker " +
            "where you can manage and organize your skincare and cosmetics.\n\n" +
            "Get Started:\n" +
            "• Add your first product\n" +
            "• Organize by category\n" +
            "• Track expiration dates\n" +
            "• Get personalized skincare advice\n" +
            "• Mark your favorites\n\n" +
            "If you have any questions or need assistance, feel free to reach out to us.\n\n" +
            "Happy organizing!\n\n" +
            "Best regards,\n" +
            "The BeautyStock Team",
            fullName
        );
    }
}
