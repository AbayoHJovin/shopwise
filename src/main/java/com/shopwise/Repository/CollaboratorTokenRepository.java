package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.CollaboratorToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CollaboratorTokenRepository extends JpaRepository<CollaboratorToken, UUID> {
    
    Optional<CollaboratorToken> findByToken(String token);
    
    @Query("SELECT ct FROM CollaboratorToken ct WHERE ct.token = :token AND ct.expiryDate > :now")
    Optional<CollaboratorToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    Optional<CollaboratorToken> findByEmailAndBusiness(String email, Business business);
    
    void deleteByExpiryDateBefore(LocalDateTime date);
}
