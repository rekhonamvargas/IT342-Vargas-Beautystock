package com.beautystock.features.profile.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProfileUpdateDTO {
    private String fullName;
    private String city;
    private String profileImageUrl;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
