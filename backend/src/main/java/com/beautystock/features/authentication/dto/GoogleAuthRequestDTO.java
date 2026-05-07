package com.beautystock.features.authentication.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequestDTO {

    @NotBlank(message = "ID token is required")
    private String idToken;

    private String ageRange;

    public GoogleAuthRequestDTO() {}

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }
}
