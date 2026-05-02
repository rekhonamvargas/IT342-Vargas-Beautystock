package com.beautystock.shared.service;

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
            if (fromEmail == null || fromEmail.isEmpty() || fromEmail.equals("${spring.mail.username}")) {
                log.warn("Email sender not configured, skipping email send to {}", toEmail);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to BeautyStock!");
            message.setText(buildWelcomeEmailBody());
            
            mailSender.send(message);
            log.info("Welcome email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage(), e);
            // Don't throw exception - allow registration to proceed even if email fails
        }
    }

    private String buildWelcomeEmailBody() {
        return "Dearest Esteemed User,\n\n" +
               "It is with great pleasure that I welcome you to *BeautyStock*—a most clever companion for keeping one's beauty treasures in perfect order.\n\n" +
               "From tracking your cherished cosmetics to offering timely skincare advice—tailored by age and even the whims of the weather—you shall find yourself wonderfully well attended.\n\n" +
               "Pray, enjoy the elegance of organization and care.\n\n" +
               "Yours sincerely,\n" +
               "BeautyStock";
    }
}
