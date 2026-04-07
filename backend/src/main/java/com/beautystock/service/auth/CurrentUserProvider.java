package com.beautystock.service.auth;

/**
 * Repository/Provider Pattern for current user abstraction.
 * Decouples business logic from Spring Security context.
 * Makes services testable without security infrastructure.
 */
public interface CurrentUserProvider {
    /**
     * Get email of currently authenticated user.
     * 
     * @return User email
     * @throws IllegalStateException if no user is authenticated
     */
    String getCurrentUserEmail();

    /**
     * Get ID of currently authenticated user.
     * 
     * @return User ID
     * @throws IllegalStateException if no user is authenticated
     */
    Long getCurrentUserId();

    /**
     * Check if user is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    boolean isAuthenticated();
}
