package com.beautystock.infrastructure.controller;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.infrastructure.scheduler.NotificationScheduler;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class NotificationTestController {

    private final NotificationScheduler scheduler;
    private final UserRepository userRepository;

    public NotificationTestController(NotificationScheduler scheduler,
                                      UserRepository userRepository) {
        this.scheduler = scheduler;
        this.userRepository = userRepository;
    }

    @GetMapping("/notify")
    public String testNotification() {

        if (userRepository.findAll().isEmpty()) {
            return "No users found in database";
        }

        User user = userRepository.findAll().get(0);

        scheduler.sendNotificationsForUserTest(user);

        return "Notification triggered!";
    }
}