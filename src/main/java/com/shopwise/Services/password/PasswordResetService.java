package com.shopwise.Services.password;

import com.shopwise.Dto.password.ForgotPasswordRequest;
import com.shopwise.Dto.password.ResetPasswordRequest;

/**
 * Service interface for password reset functionality
 */
public interface PasswordResetService {
    
    /**
     * Process a forgot password request by generating a token and sending a reset email
     * 
     * @param request The forgot password request containing the user's email
     * @return A message indicating the result of the operation
     */
    String processForgotPassword(ForgotPasswordRequest request);
    
    /**
     * Validate a password reset token
     * 
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validateResetToken(String token);
    
    /**
     * Reset a user's password using a valid token
     * 
     * @param request The reset password request containing the token and new password
     * @return A message indicating the result of the operation
     */
    String resetPassword(ResetPasswordRequest request);
}
