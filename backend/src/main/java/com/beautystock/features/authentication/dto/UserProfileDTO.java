package com.beautystock.features.authentication.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserProfileDTO {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public UserProfileDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String email;
        private String fullName;
        private String role;
        private String profileImageUrl;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder fullName(String fullName) { this.fullName = fullName; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder profileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public UserProfileDTO build() {
            UserProfileDTO dto = new UserProfileDTO();
            dto.id = id; dto.email = email; dto.fullName = fullName;
            dto.role = role; dto.profileImageUrl = profileImageUrl; dto.createdAt = createdAt;
            return dto;
        }
    }
}
