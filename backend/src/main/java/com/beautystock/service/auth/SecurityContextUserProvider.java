package com.beautystock.service.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Concrete implementation of CurrentUserProvider using Spring Security context.
 * Can be easily mocked or replaced with alternative implementations.
 */
@Component
public class SecurityContextUserProvider implements CurrentUserProvider {
    
    @Override
    public String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            throw new IllegalStateException("No authenticated user found");
        }
    }

    @Override
    public Long getCurrentUserId() {
        // Note: Spring Security principal is typically username/email
        // If you need user ID, extract from JWT claims or store in principal
        // For now, this is a placeholder
        throw new UnsupportedOperationException("User ID extraction not implemented");
    }

    @Override
    public boolean isAuthenticated() {
        try {
            return SecurityContextHolder.getContext().getAuthentication() != null
                    && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        } catch (Exception e) {
            return false;
        }
    }
}
