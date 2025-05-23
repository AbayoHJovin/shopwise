package com.shopwise.controllers;

import com.shopwise.Services.BusinessSelectionService;
import com.shopwise.Services.auth.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessSelectionController {

    private final BusinessSelectionService businessSelectionService;
    private final JwtService jwtService;

    /**
     * Endpoint to select a business as the active business for the current user
     * Sets an HttpOnly cookie with the selected business ID
     *
     * @param businessId The ID of the business to select
     * @param request HTTP request to extract user information from cookies
     * @param response HTTP response to set the business cookie
     * @return Success or error response
     */
    @PostMapping("/select/{businessId}")
    @Transactional
    public ResponseEntity<?> selectBusiness(
            @PathVariable UUID businessId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            // Get the token from the cookie
            Cookie[] cookies = request.getCookies();
            String token = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (token == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Get user email from the token
            String email = jwtService.extractClaims(token).getSubject();
            
            // Verify ownership and set the cookie
            boolean isSuccess = businessSelectionService.selectBusinessForUser(email, businessId, response);
            
            if (isSuccess) {
                Map<String, String> successResponse = new HashMap<>();
                successResponse.put("message", "Business selected successfully");
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Failed to select business");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get the currently selected business ID
     *
     * @param request HTTP request to extract the business cookie
     * @return The currently selected business ID or null
     */
    @GetMapping("/selected")
    public ResponseEntity<?> getSelectedBusiness(HttpServletRequest request) {
        try {
            Cookie[] cookies = request.getCookies();
            String selectedBusinessId = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessId = cookie.getValue();
                        break;
                    }
                }
            }
            
            Map<String, String> response = new HashMap<>();
            if (selectedBusinessId != null) {
                response.put("selectedBusinessId", selectedBusinessId);
            } else {
                response.put("selectedBusinessId", null);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Clear the selected business
     *
     * @param response HTTP response to clear the business cookie
     * @return Success response
     */
    @PostMapping("/deselect")
    public ResponseEntity<?> deselectBusiness(HttpServletResponse response) {
        try {
            // Clear the selected business cookie
            Cookie cookie = new Cookie("selectedBusiness", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0); // Expire immediately
            response.addCookie(cookie);
            
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Business deselected successfully");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
