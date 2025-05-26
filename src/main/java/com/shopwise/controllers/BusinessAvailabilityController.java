package com.shopwise.controllers;

import com.shopwise.Dto.business.BusinessAvailabilityRequest;
import com.shopwise.Dto.business.BusinessAvailabilityResponse;
import com.shopwise.Services.business.BusinessAvailabilityService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
 * Controller for managing business availability
 */
@RestController
@RequestMapping("/api/business/availability")
@RequiredArgsConstructor
@Slf4j
public class BusinessAvailabilityController {

    private final BusinessAvailabilityService businessAvailabilityService;

    /**
     * Helper method to extract business ID from cookies
     * 
     * @param request The HTTP request containing cookies
     * @return ResponseEntity with error if business not selected, or null if business ID was successfully extracted
     */
    private ResponseEntity<Map<String, String>> validateAndExtractBusinessId(HttpServletRequest request, UUID[] businessIdHolder) {
        // Extract business ID from cookie
        String businessIdStr = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("selectedBusiness".equals(cookie.getName())) {
                    businessIdStr = cookie.getValue();
                    break;
                }
            }
        }
        
        // Check if business ID is available
        if (businessIdStr == null || businessIdStr.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No business selected. Please select a business first.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        // Parse business ID
        try {
            businessIdHolder[0] = UUID.fromString(businessIdStr);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid business ID format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        return null; // No error, business ID successfully extracted
    }

    /**
     * Updates the availability status of a business
     * 
     * @param request The request containing the new availability status
     * @param httpRequest HTTP request containing cookies
     * @param authentication Authentication object with user details
     * @return The updated business availability status
     */
    @PutMapping
    public ResponseEntity<?> updateAvailability(
            @Valid @RequestBody BusinessAvailabilityRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        log.info("Received request to update business availability: {}", request.isOpen());
        
        try {
            // Extract business ID from cookies
            UUID[] businessIdHolder = new UUID[1];
            ResponseEntity<Map<String, String>> errorResponse = validateAndExtractBusinessId(httpRequest, businessIdHolder);
            if (errorResponse != null) {
                log.warn("Business validation failed when updating availability");
                return errorResponse;
            }
            UUID businessId = businessIdHolder[0];
            
            // Update the availability
            log.info("Updating availability for business ID: {}", businessId);
            BusinessAvailabilityResponse response = businessAvailabilityService.updateAvailability(businessId, request);
            
            // Return success response
            log.info("Successfully updated availability for business ID: {}", businessId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument when updating availability: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error when updating availability", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while processing your request. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gets the current availability status of a business
     * 
     * @param httpRequest HTTP request containing cookies
     * @param authentication Authentication object with user details
     * @return The current business availability status
     */
    @GetMapping
    public ResponseEntity<?> getAvailability(
            HttpServletRequest httpRequest,
            Authentication authentication) {
        log.info("Received request to get business availability");
        
        try {
            // Extract business ID from cookies
            UUID[] businessIdHolder = new UUID[1];
            ResponseEntity<Map<String, String>> errorResponse = validateAndExtractBusinessId(httpRequest, businessIdHolder);
            if (errorResponse != null) {
                log.warn("Business validation failed when getting availability");
                return errorResponse;
            }
            UUID businessId = businessIdHolder[0];
            
            // Get the availability
            log.info("Getting availability for business ID: {}", businessId);
            BusinessAvailabilityResponse response = businessAvailabilityService.getAvailability(businessId);
            
            // Return success response
            log.info("Successfully retrieved availability for business ID: {}", businessId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument when getting availability: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error when getting availability", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while processing your request. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
