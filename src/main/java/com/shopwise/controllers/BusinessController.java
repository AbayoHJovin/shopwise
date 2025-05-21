package com.shopwise.controllers;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.Request.CreateBusinessRequest;
import com.shopwise.Services.business.BusinessException;
import com.shopwise.Services.business.BusinessService;
import com.shopwise.models.User;
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

    @PostMapping
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

    @GetMapping("/{businessId}")
    public ResponseEntity<?> getBusinessById(@PathVariable UUID businessId, 
                                             Authentication authentication) {
        try {
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

    @GetMapping
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

    @PutMapping("/{businessId}")
    public ResponseEntity<?> updateBusiness(@PathVariable UUID businessId,
                                            @RequestBody BusinessDto updates,
                                            Authentication authentication) {
        try {
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
}
