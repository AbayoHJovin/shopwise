package com.shopwise.controllers;

import com.shopwise.Dto.payment.CreatePaymentRequestDto;
import com.shopwise.Dto.payment.ManualPaymentRequestDto;
import com.shopwise.Dto.payment.UpdatePaymentStatusDto;
import com.shopwise.Services.payment.ManualPaymentService;
import com.shopwise.Services.payment.PaymentException;
import com.shopwise.enums.PaymentStatus;
import com.shopwise.models.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for handling manual payment requests and subscription management
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class ManualPaymentController {

    private final ManualPaymentService paymentService;
    
    private static final String UPLOAD_DIR = "payment-screenshots";
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPaymentRequest(
            @RequestPart("screenshot") MultipartFile screenshot,
            @RequestPart("paymentDetails") @Valid CreatePaymentRequestDto requestDto,
            Authentication authentication) {
        
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            
            // Process and save the screenshot
            String fileName = processScreenshotUpload(screenshot, currentUser.getId());

            ManualPaymentRequestDto createdRequest = paymentService.createPaymentRequest(requestDto, currentUser, fileName);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
        } catch (PaymentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process payment screenshot: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all payment requests (admin only)
     */
    @GetMapping("/admin")
    public ResponseEntity<?> getAllPaymentRequests(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) UUID userId,
            Authentication authentication) {
        
        try {
            // Verify admin role
            if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only administrators can access all payment requests");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            List<ManualPaymentRequestDto> requests = paymentService.getAllPaymentRequests(status, userId);
            return ResponseEntity.ok(requests);
        } catch (PaymentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get a specific payment request by ID
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getPaymentRequestById(
            @PathVariable UUID requestId,
            Authentication authentication) {
        
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            
            // Get payment request
            ManualPaymentRequestDto request = paymentService.getPaymentRequestById(requestId);
            
            // Check if user is admin or the owner of the request
            boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            boolean isOwner = request.getSubmittedById().equals(currentUser.getId());
            
            if (!isAdmin && !isOwner) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "You don't have permission to view this payment request");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            return ResponseEntity.ok(request);
        } catch (PaymentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Update payment request status (admin only)
     */
    @PutMapping("/{requestId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable UUID requestId,
            @RequestBody @Valid UpdatePaymentStatusDto updateDto,
            Authentication authentication) {
        
        try {
            // Verify admin role
            if (!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only administrators can update payment status");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Get admin user from authentication
            User adminUser = (User) authentication.getPrincipal();
            
            // Update payment status
            ManualPaymentRequestDto updatedRequest = paymentService.updatePaymentStatus(requestId, updateDto, adminUser);
            
            return ResponseEntity.ok(updatedRequest);
        } catch (PaymentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all payment requests for the current user
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyPaymentRequests(Authentication authentication) {
        try {
            // Get current user from authentication
            User currentUser = (User) authentication.getPrincipal();
            
            // Get user's payment requests
            List<ManualPaymentRequestDto> requests = paymentService.getPaymentRequestsByUser(currentUser.getId());
            
            return ResponseEntity.ok(requests);
        } catch (PaymentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Process and save the screenshot file
     * 
     * @param screenshot The screenshot file
     * @param userId The ID of the user uploading the screenshot
     * @return The filename of the saved screenshot
     * @throws IOException If there's an error processing the file
     */
    private String processScreenshotUpload(MultipartFile screenshot, UUID userId) throws IOException {
        // Validate file
        if (screenshot.isEmpty()) {
            throw PaymentException.badRequest("Screenshot file is empty");
        }
        
        // Check file type
        String contentType = screenshot.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || 
                                     contentType.equals("image/png") || 
                                     contentType.equals("image/jpg"))) {
            throw PaymentException.badRequest("Only JPEG, JPG and PNG images are allowed");
        }
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = screenshot.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = userId + "_" + System.currentTimeMillis() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(screenshot.getInputStream(), filePath);
        
        log.info("Saved payment screenshot: {}", filename);
        
        return filename;
    }
}
