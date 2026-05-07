package com.beautystock.features.authentication.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private String profileImageUrl;
    private String googleId;
    private String notificationEmail;
    private boolean notificationsEnabled;
    private LocalDateTime createdAt;
    private String city;

    public UserProfileDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String role;
        private String profileImageUrl;
        private String googleId;
        private String notificationEmail;
        private boolean notificationsEnabled;
        private LocalDateTime createdAt;
        private String city;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }
        public Builder googleId(String googleId) { this.googleId = googleId; return this; }
        public Builder notificationEmail(String notificationEmail) { this.notificationEmail = notificationEmail; return this; }
        public Builder notificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder city(String city) { this.city = city; return this; }

        public UserProfileDTO build() {
            UserProfileDTO dto = new UserProfileDTO();
            dto.id = id; dto.email = email; dto.firstName = firstName; dto.lastName = lastName;
            dto.fullName = fullName; dto.role = role; dto.profileImageUrl = profileImageUrl;
            dto.googleId = googleId; dto.notificationEmail = notificationEmail;
            dto.notificationsEnabled = notificationsEnabled;
            dto.createdAt = createdAt; dto.city = city;
            return dto;
        }
    }
}
