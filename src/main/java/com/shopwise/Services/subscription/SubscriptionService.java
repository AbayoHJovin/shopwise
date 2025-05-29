package com.shopwise.Services.subscription;

import com.shopwise.Dto.subscription.SubscriptionInfoDto;
import com.shopwise.models.User;

import java.util.UUID;

/**
 * Service for managing user subscriptions
 */
public interface SubscriptionService {
    
    /**
     * Start a free trial for a user
     * 
     * @param userId The ID of the user to start the free trial for
     * @return The updated subscription information
     * @throws SubscriptionException if the user has already used their free trial
     */
    SubscriptionInfoDto startFreeTrial(UUID userId);
    
    /**
     * Get the subscription information for a user
     * 
     * @param userId The ID of the user to get subscription information for
     * @return The user's subscription information
     */
    SubscriptionInfoDto getSubscriptionInfo(UUID userId);
    
    /**
     * Check if a user is eligible for a free trial
     * 
     * @param user The user to check
     * @return true if the user is eligible for a free trial, false otherwise
     */
    boolean isEligibleForFreeTrial(User user);
}
