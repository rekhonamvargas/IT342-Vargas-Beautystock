package com.beautystock.infrastructure.controller;

import com.beautystock.features.authentication.entity.User;
import com.beautystock.features.authentication.repository.UserRepository;
import com.beautystock.infrastructure.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Mobile OAuth callback handler
 * Provides JWT tokens for mobile apps after OAuth2 authentication
 */
@RestController
@RequestMapping("/oauth2/mobile")
public class MobileOAuthController {

    private static final String REDIRECT_SCHEME_COOKIE = "beautystock_mobile_redirect_scheme";
    private static final String REDIRECT_HOST_COOKIE = "beautystock_mobile_redirect_host";
    private static final String REDIRECT_PATH_COOKIE = "beautystock_mobile_redirect_path";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${app.oauth2.redirect-success-url:http://localhost:3002/oauth2/callback}")
    private String webRedirectSuccessUrl;

    @GetMapping("/google")
    public void startMobileGoogleOAuth(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "redirect_scheme", defaultValue = "beautystock") String redirectScheme,
            @RequestParam(value = "redirect_host", defaultValue = "oauth2") String redirectHost,
            @RequestParam(value = "redirect_path", defaultValue = "callback") String redirectPath) throws IOException {

        String normalizedPath = redirectPath.startsWith("/") ? redirectPath : "/" + redirectPath;

        addRedirectCookie(response, REDIRECT_SCHEME_COOKIE, redirectScheme, request.getContextPath());
        addRedirectCookie(response, REDIRECT_HOST_COOKIE, redirectHost, request.getContextPath());
        addRedirectCookie(response, REDIRECT_PATH_COOKIE, normalizedPath, request.getContextPath());

        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/google");
    }

    /**
     * Get JWT token for authenticated user (used after OAuth2 flow)
     * Returns token and metadata for mobile apps
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> getOAuthToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth"));

        String token = jwtTokenProvider.generateToken(user);
        boolean isNewUser = user.getRole() == null || user.getRole().name().isEmpty();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("isNewUser", isNewUser);
        response.put("email", user.getEmail());
        response.put("name", user.getFullName());

        return ResponseEntity.ok(response);
    }

    /**
     * Redirect endpoint for mobile OAuth - constructs custom URI
     */
    @GetMapping("/redirect")
    public ResponseEntity<Map<String, String>> getMobileRedirect(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "isNewUser", defaultValue = "false") boolean isNewUser) {

        Map<String, String> response = new HashMap<>();

        if (error != null && !error.isEmpty()) {
            response.put("redirectUri", "beautystock://oauth2/callback?error=" + java.net.URLEncoder.encode(error));
        } else if (token != null && !token.isEmpty()) {
            response.put("redirectUri", 
                    "beautystock://oauth2/callback?token=" + token + "&isNewUser=" + isNewUser);
        } else {
            response.put("redirectUri", 
                    "beautystock://oauth2/callback?error=" + 
                    java.net.URLEncoder.encode("Invalid OAuth response"));
        }

        return ResponseEntity.ok(response);
    }

    private void addRedirectCookie(HttpServletResponse response, String name, String value, String path) {
        Cookie cookie = new Cookie(name, value == null ? "" : value);
        cookie.setPath(path == null || path.isBlank() ? "/" : path);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(300);
        response.addCookie(cookie);
    }
}
