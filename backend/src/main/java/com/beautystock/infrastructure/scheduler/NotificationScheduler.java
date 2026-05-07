package com.beautystock.infrastructure.scheduler;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.features.products.entity.Product;
import com.beautystock.features.products.repository.ProductRepository;
import com.beautystock.infrastructure.service.EmailService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public NotificationScheduler(UserRepository userRepository,
                                 ProductRepository productRepository,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    // 🔥 FOR TESTING (every minute)
    // change back to 9AM later
    @Scheduled(cron = "0 50 3 * * ?")
    public void sendExpirationNotifications() {

        logger.info("🌸 Scheduler running...");

        List<User> users = userRepository.findAll();

        for (User user : users) {

            if (!user.isNotificationsEnabled()) continue;

            sendNotificationsForUser(user);
        }

        logger.info("✅ Scheduler finished");
    }

    private void sendNotificationsForUser(User user) {

        LocalDate today = LocalDate.now();
        LocalDate future = today.plusDays(15);

        List<Product> products = productRepository
            .findByOwnerEmailAndExpirationDateBetweenOrderByExpirationDateAsc(
                user.getEmail(), today, future);

        for (Product product : products) {

            if (product.getExpirationDate() == null) continue;

            long days = java.time.temporal.ChronoUnit.DAYS
                .between(today, product.getExpirationDate());

            logger.info("📦 {} | {} days left", product.getName(), days);

            // 🎯 ONLY THESE DAYS
            if (days == 15 || days == 7 || days == 3 || days == 1) {

                String message = buildMessage(product.getName(), (int) days);

                logger.info(message);

                emailService.sendCustomEmail(
                    user.getEmail(),
                    "A Most Urgent Beauty Notice ✨",
                    message
                );

                logger.info("📧 Email sent to {}", user.getEmail());
            }
        }
    }

    // ✅ KEEP (for your controllers)
    public void sendNotificationsForUserTest(User user) {
        sendNotificationsForUser(user);
    }

    // 💌 YOUR BRIDGERTON MESSAGE
    private String buildMessage(String productName, int days) {

        return String.format(
            "My Dearest Beauty Enthusiast,\n\n" +
            "It has come to our most delicate attention that your cherished possession '%s'\n" +
            "shall reach its expiration in %d day(s).\n\n" +
            "Such matters require the utmost urgency, for beauty waits for no one.\n\n" +
            "Pray, attend to your collection with grace and haste.\n\n" +
            "Yours in elegance,\n" +
            "The BeautyStock Society ✨",
            productName, days
        );
    }
}