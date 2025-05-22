package com.shopwise.controllers;

import com.shopwise.Dto.availability.AvailabilitySlotRequest;
import com.shopwise.Dto.availability.AvailabilitySlotResponse;
import com.shopwise.Services.availability.AvailabilitySlotException;
import com.shopwise.Services.availability.AvailabilitySlotService;
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
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilitySlotController {

    private final AvailabilitySlotService availabilitySlotService;

    /**
     * Adds a new availability slot for a business
     * 
     * @param businessId Business ID
     * @param request Availability slot details
     * @param authentication Authentication object with user details
     * @return Created availability slot
     */
    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> addAvailability(
            @PathVariable UUID businessId,
            @Valid @RequestBody AvailabilitySlotRequest request,
            Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
            AvailabilitySlotResponse createdSlot = availabilitySlotService.addAvailability(businessId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSlot);
        } catch (AvailabilitySlotException e) {
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
     * Gets all availability slots for a business
     * 
     * @param businessId Business ID
     * @param authentication Authentication object with user details
     * @return List of availability slots for the business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getAvailability(
            @PathVariable UUID businessId,
            Authentication authentication) {
        try {
            List<AvailabilitySlotResponse> slots = availabilitySlotService.getAvailability(businessId);
            return ResponseEntity.ok(slots);
        } catch (AvailabilitySlotException e) {
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
     * Deletes an availability slot
     * 
     * @param slotId Availability slot ID
     * @param authentication Authentication object with user details
     * @return Success message
     */
    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteAvailability(
            @PathVariable UUID slotId,
            Authentication authentication) {
        try {
            availabilitySlotService.deleteAvailability(slotId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Availability slot deleted successfully");
            return ResponseEntity.ok(response);
        } catch (AvailabilitySlotException e) {
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
