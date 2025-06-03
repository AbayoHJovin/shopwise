package com.shopwise.Services.password;

import com.shopwise.Dto.password.ForgotPasswordRequest;
import com.shopwise.Dto.password.ResetPasswordRequest;
import com.shopwise.Repository.PasswordResetTokenRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.email.EmailService;
import com.shopwise.models.PasswordResetToken;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Token expiration time in minutes (default: 30 minutes)
    @Value("${app.password-reset.token-expiry:30}")
    private int tokenExpiryMinutes;

    @Override
    @Transactional
    public String processForgotPassword(ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail());
        
        // Always return a generic message to avoid leaking user information
        String genericMessage = "If your email is registered, you will receive a password reset link shortly.";
        
        // If user not found, return generic message (for security)
        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", request.getEmail());
            return genericMessage;
        }
        
        // Check for existing token
        Optional<PasswordResetToken> existingToken = tokenRepository.findByEmail(request.getEmail());
        
        // Delete existing token if found
        existingToken.ifPresent(tokenRepository::delete);
        
        // Generate new token
        String token = generateUniqueToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(tokenExpiryMinutes);
        
        // Create and save new token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setEmail(request.getEmail());
        resetToken.setToken(token);
        resetToken.setExpirationTime(expiryTime);
        resetToken.setUser(user);
        tokenRepository.save(resetToken);
        
        // Generate reset link
        // Frontend URL for password reset page
        String frontendUrl = "https://shopwise-self.vercel.app";
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        try {
            emailService.sendPasswordResetEmail(request.getEmail(), resetLink, user.getName());
            log.info("Password reset email sent to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", request.getEmail(), e);
        }
        
        return genericMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        return tokenRepository.findValidToken(token, LocalDateTime.now()).isPresent();
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        // Validate token
        PasswordResetToken resetToken = tokenRepository.findValidToken(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> PasswordResetException.unauthorized("Invalid or expired token"));
        
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw PasswordResetException.badRequest("Passwords do not match");
        }
        
        // Find user
        User user = resetToken.getUser();
        if (user == null) {
            throw PasswordResetException.notFound("User not found");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        
        // Delete token to prevent reuse
        tokenRepository.delete(resetToken);
        
        return "Password has been reset successfully";
    }
    
    /**
     * Generate a unique token for password reset
     * 
     * @return A unique token string
     */
    private String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }
}
