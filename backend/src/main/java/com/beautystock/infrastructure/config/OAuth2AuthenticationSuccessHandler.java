package com.beautystock.infrastructure.config;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.entity.UserRole;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.infrastructure.util.JwtTokenProvider;
import com.beautystock.infrastructure.service.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REDIRECT_SCHEME_COOKIE = "beautystock_mobile_redirect_scheme";
    private static final String REDIRECT_HOST_COOKIE = "beautystock_mobile_redirect_host";
    private static final String REDIRECT_PATH_COOKIE = "beautystock_mobile_redirect_path";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.oauth2.redirect-success-url:http://localhost:3000/oauth2/callback}")
    private String redirectSuccessUrl;

    @Value("${app.oauth2.redirect-failure-url:http://localhost:3000/login}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            String givenName = oAuth2User.getAttribute("given_name");
            String familyName = oAuth2User.getAttribute("family_name");
            String picture = oAuth2User.getAttribute("picture");
            String googleId = oAuth2User.getAttribute("sub");

            // Validate required fields
            if (email == null || email.isEmpty() || googleId == null || googleId.isEmpty()) {
                String error = "Missing required Google OAuth attributes: email=" + (email == null ? "null" : email) + 
                               ", googleId=" + (googleId == null ? "null" : googleId);
                redirectToClientWithError(request, response, error);
                return;
            }

            // Check if googleId is already linked to a different user (duplicate account detection)
            java.util.Optional<User> existingGoogleUser = userRepository.findByGoogleId(googleId);
            if (existingGoogleUser.isPresent() && !existingGoogleUser.get().getEmail().equals(email)) {
                // This Google account is already linked to a different email
                String errorMsg = "This Google account is already registered with email: " + existingGoogleUser.get().getEmail() + 
                               ". Please use that email or sign up with a different Google account.";
                redirectToClientWithError(request, response, errorMsg);
                return;
            }

            // Check if user exists by email
            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            boolean isNewUser = false;
            if (user == null) {
                // Create new user - don't set role yet, let user choose
                isNewUser = true;
                user = new User();
                user.setEmail(email);
                user.setFirstName(givenName != null ? givenName : "");
                user.setLastName(familyName != null ? familyName : "");
                String fullName = ((givenName != null ? givenName : "") + " " + 
                                  (familyName != null ? familyName : "")).trim();
                user.setFullName(fullName.isEmpty() ? email : fullName);
                user.setProfileImageUrl(picture);
                user.setGoogleId(googleId);
                user.setPassword(""); // Placeholder for OAuth2 users
                user.setEmailVerified(true);
                user.setRole(UserRole.ROLE_ADULT); // Temporary default - will be changed by user
                // Automatically set notification email to user's Google email and enable notifications
                user.setNotificationEmail(email);
                user.setNotificationsEnabled(true);
                userRepository.save(user);
                
                // Send welcome email for new users
                try {
                    emailService.sendWelcomeEmail(email, user.getFullName());
                } catch (Exception e) {
                    // Log but don't fail if email sending fails
                    System.err.println("Failed to send welcome email: " + e.getMessage());
                }
            } else {
                // For existing users, ensure googleId is linked for cross-device login
                boolean existingUserUpdated = false;
                if (user.getGoogleId() == null || user.getGoogleId().isEmpty()) {
                    user.setGoogleId(googleId);
                    existingUserUpdated = true;
                }
                if (user.getRole() == null) {
                    user.setRole(UserRole.ROLE_ADULT);
                    existingUserUpdated = true;
                }
                // If this is their first Google connection, set notification email
                if (user.getNotificationEmail() == null || user.getNotificationEmail().isEmpty()) {
                    user.setNotificationEmail(email);
                    user.setNotificationsEnabled(true);
                    existingUserUpdated = true;
                }
                if (existingUserUpdated) {
                    userRepository.save(user);
                }
                // Send login notification email for returning users
                try {
                    emailService.sendLoginNotificationEmail(email, user.getFullName());
                } catch (Exception e) {
                    // Log but don't fail if email sending fails
                    System.err.println("Failed to send login notification email: " + e.getMessage());
                }
            }

            // Generate JWT token (same structure as email/password login)
            String token = jwtTokenProvider.generateToken(user);

            // Determine redirect URL (mobile or web)
            String redirectUri = determineRedirectUrl(request, token, isNewUser);
            clearRedirectCookies(request, response);
            response.sendRedirect(redirectUri);
        } catch (Exception e) {
            throw new ServletException("OAuth2 authentication failed", e);
        }
    }

    /**
     * Redirect error to client (mobile or web)
     */
    private void redirectToClientWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws IOException {
        String scheme = resolveRedirectValue(request, REDIRECT_SCHEME_COOKIE, "redirect_scheme");
        String host = resolveRedirectValue(request, REDIRECT_HOST_COOKIE, "redirect_host");
        String path = resolveRedirectValue(request, REDIRECT_PATH_COOKIE, "redirect_path");

        if (scheme != null && !scheme.isBlank() && host != null && !host.isBlank()) {
            // Mobile: custom scheme redirect
            String redirectPath = path == null || path.isBlank() ? "" : path;
            if (!redirectPath.startsWith("/")) {
                redirectPath = "/" + redirectPath;
            }
            String redirectUri = scheme + "://" + host + redirectPath + "?error=" + 
                    java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8);
            clearRedirectCookies(request, response);
            response.sendRedirect(redirectUri);
        } else {
            // Web: standard redirect
            clearRedirectCookies(request, response);
            response.sendRedirect(redirectFailureUrl + "?error=" + 
                    java.net.URLEncoder.encode(errorMessage, java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Determine correct redirect URL based on client type (mobile vs web)
     * Mobile: beautystock://oauth2/callback?token=...&isNewUser=...
    * Web: http://localhost:3000/oauth2/callback?token=...&isNewUser=...
     */
    private String determineRedirectUrl(HttpServletRequest request, String token, boolean isNewUser) {
        String scheme = resolveRedirectValue(request, REDIRECT_SCHEME_COOKIE, "redirect_scheme");
        String host = resolveRedirectValue(request, REDIRECT_HOST_COOKIE, "redirect_host");
        String path = resolveRedirectValue(request, REDIRECT_PATH_COOKIE, "redirect_path");

        if (scheme != null && !scheme.isBlank() && host != null && !host.isBlank()) {
            // Mobile: construct custom scheme URI
            String redirectPath = path == null || path.isBlank() ? "" : path;
            if (!redirectPath.startsWith("/")) {
                redirectPath = "/" + redirectPath;
            }
            return scheme + "://" + host + redirectPath + "?token=" + 
                    java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8) + 
                    "&isNewUser=" + isNewUser;
        } else {
            // Web: standard web URL redirect
            return redirectSuccessUrl + "?token=" + 
                    java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8) + 
                    "&isNewUser=" + isNewUser;
        }
    }

    private String resolveRedirectValue(HttpServletRequest request, String cookieName, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null && !value.isBlank()) {
            return value;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                String cookieValue = cookie.getValue();
                if (cookieValue == null || cookieValue.isBlank()) {
                    return null;
                }
                return URLDecoder.decode(cookieValue, StandardCharsets.UTF_8);
            }
        }

        return null;
    }

    private void clearRedirectCookies(HttpServletRequest request, HttpServletResponse response) {
        String cookiePath = request.getContextPath();
        if (cookiePath == null || cookiePath.isBlank()) {
            cookiePath = "/";
        }

        addExpiredCookie(response, REDIRECT_SCHEME_COOKIE, cookiePath);
        addExpiredCookie(response, REDIRECT_HOST_COOKIE, cookiePath);
        addExpiredCookie(response, REDIRECT_PATH_COOKIE, cookiePath);
    }

    private void addExpiredCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}


