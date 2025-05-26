package com.shopwise.Services.business;

import com.shopwise.Dto.business.BusinessAvailabilityRequest;
import com.shopwise.Dto.business.BusinessAvailabilityResponse;

import java.util.UUID;

/**
 * Service interface for managing business availability
 */
public interface BusinessAvailabilityService {
    
    /**
     * Updates the availability status of a business
     * 
     * @param businessId The ID of the business to update
     * @param request The request containing the new availability status
     * @return The updated business availability status
     */
    BusinessAvailabilityResponse updateAvailability(UUID businessId, BusinessAvailabilityRequest request);
    
    /**
     * Gets the current availability status of a business
     * 
     * @param businessId The ID of the business to check
     * @return The current business availability status
     */
    BusinessAvailabilityResponse getAvailability(UUID businessId);
}
