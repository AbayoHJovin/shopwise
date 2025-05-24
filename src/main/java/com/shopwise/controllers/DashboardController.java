package com.shopwise.controllers;

import com.shopwise.Dto.dashboard.BusinessDashboardDto;
import com.shopwise.Services.dashboard.DashboardService;
import com.shopwise.Services.auth.JwtService;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling dashboard-related requests
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtService jwtService;

    /**
     * Get dashboard data for the currently selected business
     *
     * @param request HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return Dashboard data for the selected business
     */
    
    @GetMapping
    public ResponseEntity<?> getDashboard(HttpServletRequest request, Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the selected business ID from cookies
            Cookie[] cookies = request.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                // Convert string to UUID
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Get dashboard data
                BusinessDashboardDto dashboardData = dashboardService.getDashboardData(businessId, currentUser);
                
                return ResponseEntity.ok(dashboardData);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
        } catch (SecurityException e) {
            // User doesn't have access to the business
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to access this business dashboard"));
        } catch (Exception e) {
            log.error("Error retrieving dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
