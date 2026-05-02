package com.beautystock.shared.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.entity.UserRole;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.shared.util.JwtTokenProvider;
import com.beautystock.shared.service.EmailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.oauth2.redirect-success-url:http://localhost:3004/oauth2/callback}")
    private String redirectSuccessUrl;

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

            // Check if user exists
            User user = userRepository.findByEmail(email).orElse(null);
            boolean isNewUser = false;
            if (user == null) {
                // Create new user - don't set role yet, let user choose
                isNewUser = true;
                user = new User();
                user.setEmail(email);
                String fullName = ((givenName != null ? givenName : "") + " " + 
                                  (familyName != null ? familyName : "")).trim();
                user.setFullName(fullName.isEmpty() ? email : fullName);
                user.setProfileImageUrl(picture);
                user.setGoogleId(googleId);
                user.setPassword(""); // Placeholder for OAuth2 users
                user.setEmailVerified(true);
                user.setRole(UserRole.ROLE_ADULT); // Temporary default - will be changed by user
                userRepository.save(user);
                
                // Send welcome email for new users
                emailService.sendWelcomeEmail(email, user.getFullName());
            } else {
                // Send login notification email for returning users
                emailService.sendLoginNotificationEmail(email, user.getFullName());
            }

            // Generate JWT token (same structure as email/password login)
            String token = jwtTokenProvider.generateToken(user);

            // Redirect to frontend with token and flag for new users to select role
            String redirectUrl = redirectSuccessUrl + "?token=" + token + "&isNewUser=" + isNewUser;
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            throw new ServletException("OAuth2 authentication failed", e);
        }
    }
}
