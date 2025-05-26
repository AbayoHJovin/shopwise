package com.shopwise.Repository;

import com.shopwise.models.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Find a valid (non-expired) token by its value
     * 
     * @param token The token value
     * @param now Current time
     * @return Optional containing the token if found and valid
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.expirationTime > :now")
    Optional<PasswordResetToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Find a token by user's email
     * 
     * @param email The user's email
     * @return Optional containing the token if found
     */
    Optional<PasswordResetToken> findByEmail(String email);
    
    /**
     * Delete all tokens for a specific user
     * 
     * @param email The user's email
     */
    void deleteByEmail(String email);
}
