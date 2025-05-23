package com.shopwise.Services;

import jakarta.servlet.http.HttpServletResponse;

import java.util.UUID;

/**
 * Service for handling business selection operations
 */
public interface BusinessSelectionService {
    
    /**
     * Select a business for a user and set it as the active business
     * 
     * @param userEmail The email of the user
     * @param businessId The ID of the business to select
     * @param response HTTP response to set the cookie
     * @return true if successful, false otherwise
     * @throws IllegalArgumentException if the business is not found or not owned by the user
     */
    boolean selectBusinessForUser(String userEmail, UUID businessId, HttpServletResponse response);
    
    /**
     * Get the businesses owned by a user
     * 
     * @param userEmail The email of the user
     * @return List of businesses owned by the user
     */
    java.util.List<com.shopwise.Dto.BusinessDto> getBusinessesForUser(String userEmail);
}
