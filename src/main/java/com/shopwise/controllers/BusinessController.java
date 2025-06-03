package com.shopwise.controllers;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.Request.CreateBusinessRequest;
import com.shopwise.Dto.business.BusinessDeleteRequest;
import com.shopwise.Dto.business.BusinessUpdateRequest;
import com.shopwise.Services.business.BusinessException;
import com.shopwise.Services.business.BusinessService;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/create")
    public ResponseEntity<?> createBusiness(@Valid @RequestBody CreateBusinessRequest request, 
                                            Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            BusinessDto createdBusiness = businessService.createBusiness(request, currentUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBusiness);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/get-by-id")
    public ResponseEntity<?> getBusinessById(HttpServletRequest httpRequest,
                                             Authentication authentication) {
        try {
            String businessIdStr = null;
            Cookie[] cookies = httpRequest.getCookies();
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
            UUID businessId= UUID.fromString(businessIdStr);
            User currentUser = (User) authentication.getPrincipal();
            BusinessDto business = businessService.getBusinessById(businessId, currentUser);
            
            return ResponseEntity.ok(business);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getMine")
    public ResponseEntity<?> getUserBusinesses(Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            List<BusinessDto> businesses = businessService.getBusinessesForUser(currentUser);
            
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Updates business information using the selected business from cookies
     * 
     * @param updates Business update request with fields to update
     * @param httpRequest HTTP request containing cookies
     * @param authentication Authentication object with user details
     * @return Updated business information
     */
    @PatchMapping("/update")
    public ResponseEntity<?> updateBusiness(
            @RequestBody BusinessUpdateRequest updates,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Extract business ID from cookies
            String businessIdStr = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No business selected. Please select a business first.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            UUID businessId = UUID.fromString(businessIdStr);
            User currentUser = (User) authentication.getPrincipal();
            
            BusinessDto updatedBusiness = businessService.updateBusiness(businessId, updates, currentUser);
            
            return ResponseEntity.ok(updatedBusiness);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{businessId}/invite")
    public ResponseEntity<?> inviteCollaborator(@PathVariable UUID businessId,
                                               @RequestParam String email,
                                               Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            businessService.inviteCollaborator(businessId, email, currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invitation sent successfully");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<?> acceptInvitation(@RequestParam String token,
                                             Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            businessService.acceptCollaboration(token, currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Invitation accepted successfully");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{businessId}/collaborators")
    public ResponseEntity<?> listCollaborators(@PathVariable UUID businessId,
                                              Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            List<User> collaborators = businessService.listCollaborators(businessId, currentUser);
            
            return ResponseEntity.ok(collaborators);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{businessId}/collaborators/{userId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable UUID businessId,
                                               @PathVariable UUID userId,
                                               Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            businessService.removeCollaborator(businessId, userId, currentUser);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Collaborator removed successfully");
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Deletes a business if the requester is the owner and password verification succeeds
     * The business ID is extracted from the "selectedBusiness" cookie
     * 
     * @param deleteRequest The deletion request containing password for verification
     * @param httpRequest HTTP request containing cookies
     * @param httpResponse HTTP response for clearing cookies after deletion
     * @param authentication Authentication object with user details
     * @return A message confirming the deletion
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteBusiness(
            @Valid @RequestBody BusinessDeleteRequest deleteRequest,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            Authentication authentication) {
        try {
            // Extract business ID from cookies
            String businessIdStr = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No business selected. Please select a business first.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            UUID businessId = UUID.fromString(businessIdStr);
            User currentUser = (User) authentication.getPrincipal();
            
            // Call the service to delete the business
            String result = businessService.deleteBusiness(businessId, deleteRequest, currentUser);
            
            // Clear the selectedBusiness cookie since the business has been deleted
            Cookie cookie = new Cookie("selectedBusiness", null);
            cookie.setPath("/");
            cookie.setAttribute("SameSite", "None"); // Required for cross-origin cookies
            cookie.setSecure(true); // Ensure cookie is only sent over HTTPS
            cookie.setMaxAge(0); // Expire immediately
            httpResponse.addCookie(cookie);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
