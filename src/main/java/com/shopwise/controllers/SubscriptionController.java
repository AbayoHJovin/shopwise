package com.shopwise.controllers;

import com.shopwise.Dto.subscription.SubscriptionInfoDto;
import com.shopwise.Services.subscription.SubscriptionException;
import com.shopwise.Services.subscription.SubscriptionService;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for managing user subscriptions
 */
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Start a free trial for the authenticated user
     * 
     * @param authentication The authentication object containing the user details
     * @return The updated subscription information
     */
    @PostMapping("/free-trial")
    public ResponseEntity<?> startFreeTrial(Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Start the free trial
            SubscriptionInfoDto subscriptionInfo = subscriptionService.startFreeTrial(currentUser.getId());
            
            // Return the updated subscription info
            return ResponseEntity.status(HttpStatus.OK).body(subscriptionInfo);
        } catch (SubscriptionException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error starting free trial", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get the subscription information for the authenticated user
     * 
     * @param authentication The authentication object containing the user details
     * @return The user's subscription information
     */
    @GetMapping("/info")
    public ResponseEntity<?> getSubscriptionInfo(Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the subscription info
            SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscriptionInfo(currentUser.getId());
            
            // Return the subscription info
            return ResponseEntity.status(HttpStatus.OK).body(subscriptionInfo);
        } catch (SubscriptionException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error getting subscription info", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Check if the authenticated user is eligible for a free trial
     * 
     * @param authentication The authentication object containing the user details
     * @return true if the user is eligible for a free trial, false otherwise
     */
    @GetMapping("/free-trial/eligibility")
    public ResponseEntity<?> checkFreeTrialEligibility(Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Check if the user is eligible for a free trial
            boolean isEligible = subscriptionService.isEligibleForFreeTrial(currentUser);
            
            // Return the eligibility status
            Map<String, Boolean> response = new HashMap<>();
            response.put("eligible", isEligible);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            log.error("Unexpected error checking free trial eligibility", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
