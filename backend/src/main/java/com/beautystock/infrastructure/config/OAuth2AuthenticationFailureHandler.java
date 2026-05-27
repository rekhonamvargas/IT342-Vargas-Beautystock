package com.beautystock.infrastructure.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final String REDIRECT_SCHEME_COOKIE = "beautystock_mobile_redirect_scheme";
    private static final String REDIRECT_HOST_COOKIE = "beautystock_mobile_redirect_host";
    private static final String REDIRECT_PATH_COOKIE = "beautystock_mobile_redirect_path";

    @Value("${app.oauth2.redirect-failure-url:http://localhost:3004/login}")
    private String redirectFailureUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = exception.getMessage();
        
        // Check for mobile redirect parameters
        String scheme = resolveRedirectValue(request, REDIRECT_SCHEME_COOKIE, "redirect_scheme");
        String host = resolveRedirectValue(request, REDIRECT_HOST_COOKIE, "redirect_host");
        String path = resolveRedirectValue(request, REDIRECT_PATH_COOKIE, "redirect_path");

        String redirectUrl;
        if (scheme != null && !scheme.isBlank() && host != null && !host.isBlank()) {
            // Mobile: construct custom scheme URI
            String redirectPath = path == null || path.isBlank() ? "" : path;
            if (!redirectPath.startsWith("/")) {
                redirectPath = "/" + redirectPath;
            }
            redirectUrl = scheme + "://" + host + redirectPath + "?error=" + 
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        } else {
            // Web: standard web URL redirect
            redirectUrl = redirectFailureUrl + "?error=" + 
                    URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        }

        clearRedirectCookies(request, response);

        response.sendRedirect(redirectUrl);
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

