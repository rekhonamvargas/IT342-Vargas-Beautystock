package com.beautystock.dto;

public class AuthResponseDTO {
    private String token;
    private String refreshToken;
    private UserProfileDTO user;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token, String refreshToken, UserProfileDTO user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public UserProfileDTO getUser() { return user; }
    public void setUser(UserProfileDTO user) { this.user = user; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String token;
        private String refreshToken;
        private UserProfileDTO user;

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public Builder user(UserProfileDTO user) {
            this.user = user;
            return this;
        }

        public AuthResponseDTO build() {
            return new AuthResponseDTO(token, refreshToken, user);
        }
    }
}
