package com.shopwise.controllers;

import com.shopwise.Dto.password.ForgotPasswordRequest;
import com.shopwise.Dto.password.ResetPasswordRequest;
import com.shopwise.Services.password.PasswordResetException;
import com.shopwise.Services.password.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling password reset operations
 */
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    
    /**
     * Process a forgot password request
     * 
     * @param request The forgot password request containing the user's email
     * @return Response with a message indicating the result
     */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String message = passwordResetService.processForgotPassword(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing forgot password request", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Validate a password reset token
     * 
     * @param token The token to validate
     * @return Response indicating if the token is valid
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("valid", isValid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating reset token", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while validating the token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Reset a user's password
     * 
     * @param request The reset password request containing the token and new password
     * @return Response with a message indicating the result
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String message = passwordResetService.resetPassword(request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (PasswordResetException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Error resetting password", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An error occurred while resetting your password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
