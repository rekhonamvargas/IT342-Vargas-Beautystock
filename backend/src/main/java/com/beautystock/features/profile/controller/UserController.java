package com.beautystock.features.profile.controller;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.features.authentication.dto.UserProfileDTO;
import com.beautystock.infrastructure.scheduler.NotificationScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserRepository userRepository;
    private final NotificationScheduler notificationScheduler;

    public UserController(UserRepository userRepository, NotificationScheduler notificationScheduler) {
        this.userRepository = userRepository;
        this.notificationScheduler = notificationScheduler;
    }

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /** GET /users/me - Get current user profile */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .map(user -> {
                    UserProfileDTO profile = new UserProfileDTO();
                    profile.setId(user.getId());
                    profile.setEmail(user.getEmail());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setGoogleId(user.getGoogleId());
                    profile.setNotificationEmail(user.getNotificationEmail());
                    profile.setNotificationsEnabled(user.isNotificationsEnabled());
                    profile.setCreatedAt(user.getCreatedAt());
                    profile.setCity(user.getCity());
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /users/me/profile - Update user profile (firstName, lastName) */
    @PutMapping("/me/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        return userRepository.findByEmail(email)
                .map(user -> {
                    String firstName = request.get("firstName");
                    String lastName = request.get("lastName");
                    
                    if (firstName != null && !firstName.isBlank()) {
                        user.setFirstName(firstName.trim());
                    }
                    if (lastName != null && !lastName.isBlank()) {
                        user.setLastName(lastName.trim());
                    }
                    
                    userRepository.save(user);
                    
                    UserProfileDTO profile = new UserProfileDTO();
                    profile.setId(user.getId());
                    profile.setEmail(user.getEmail());
                    profile.setFirstName(user.getFirstName());
                    profile.setLastName(user.getLastName());
                    profile.setFullName(user.getFullName());
                    profile.setRole(user.getRole().name());
                    profile.setProfileImageUrl(user.getProfileImageUrl());
                    profile.setGoogleId(user.getGoogleId());
                    profile.setNotificationEmail(user.getNotificationEmail());
                    profile.setNotificationsEnabled(user.isNotificationsEnabled());
                    profile.setCreatedAt(user.getCreatedAt());
                    profile.setCity(user.getCity());
                    
                    return ResponseEntity.ok((Object) profile);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /users/me/location - Update user city */
    @PutMapping("/me/location")
    public ResponseEntity<?> updateLocation(@RequestBody Map<String, String> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        String city = request.get("city");

        if (city == null || city.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "City is required"));
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setCity(city.trim());
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of(
                            "message", "City updated successfully",
                            "city", user.getCity()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /users/me/profile-image - Upload profile picture */
    @PostMapping("/me/profile-image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Image file is required"));
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    try {
                        // Create uploads directory if it doesn't exist
                        Path uploadDir = Paths.get("uploads/profiles");
                        Files.createDirectories(uploadDir);
                        
                        // Generate unique filename
                        String filename = user.getId().toString() + "_" + UUID.randomUUID().toString() + 
                                         (file.getOriginalFilename() != null ? 
                                          file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : ".jpg");
                        
                        // Save file
                        Path filepath = uploadDir.resolve(filename);
                        Files.write(filepath, file.getBytes());
                        
                        // Store path in database (without /api prefix, frontend adds it)
                        String imageUrl = "/uploads/profiles/" + filename;
                        user.setProfileImageUrl(imageUrl);
                        userRepository.save(user);

                        UserProfileDTO profile = new UserProfileDTO();
                        profile.setId(user.getId());
                        profile.setEmail(user.getEmail());
                        profile.setFirstName(user.getFirstName());
                        profile.setLastName(user.getLastName());
                        profile.setFullName(user.getFullName());
                        profile.setRole(user.getRole().name());
                        profile.setProfileImageUrl(user.getProfileImageUrl());
                        profile.setGoogleId(user.getGoogleId());
                        profile.setNotificationEmail(user.getNotificationEmail());
                        profile.setNotificationsEnabled(user.isNotificationsEnabled());
                        profile.setCreatedAt(user.getCreatedAt());
                        profile.setCity(user.getCity());

                        return ResponseEntity.ok((Object) profile);
                    } catch (IOException e) {
                        logger.error("Failed to process profile image", e);
                        return ResponseEntity.badRequest().body(Map.of("message", "Failed to process image"));
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /users/me/notifications - Get notification settings */
    @GetMapping("/me/notifications")
    public ResponseEntity<?> getNotificationSettings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(Map.of(
                        "notificationEmail", user.getNotificationEmail(),
                        "notificationsEnabled", user.isNotificationsEnabled(),
                        "isGoogleConnected", user.getGoogleId() != null && !user.getGoogleId().isEmpty()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /** PUT /users/me/notifications - Update notification settings */
    @PutMapping("/me/notifications")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody Map<String, Object> request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .map(user -> {
                    // Only update notificationsEnabled flag
                    // Notification email is automatically set from user's Google email
                    if (request.containsKey("notificationsEnabled")) {
                        user.setNotificationsEnabled((Boolean) request.get("notificationsEnabled"));
                    }

                    userRepository.save(user);

                    return ResponseEntity.ok(Map.of(
                            "message", "Notification settings updated successfully",
                            "notificationEmail", user.getEmail(),
                            "notificationsEnabled", user.isNotificationsEnabled()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** POST /users/me/test-notification - Test send notification email */
    @PostMapping("/me/test-notification")
    public ResponseEntity<?> testSendNotification() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getEmail() == null || user.getEmail().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "message", "User email not found"
                        ));
                    }

                    if (user.getGoogleId() == null || user.getGoogleId().isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "message", "Google account not connected. Please connect your Google account first."
                        ));
                    }

                    // Trigger notification sending for this user
                    notificationScheduler.sendNotificationsForUserTest(user);

                    return ResponseEntity.ok(Map.of(
                        "message", "Test notification triggered. Check your email at: " + user.getEmail(),
                        "email", user.getEmail()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
