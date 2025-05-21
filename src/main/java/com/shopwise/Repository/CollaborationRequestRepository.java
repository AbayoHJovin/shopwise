package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.CollaborationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, UUID> {
    
    Optional<CollaborationRequest> findByToken(String token);
    
    Optional<CollaborationRequest> findByEmailAndBusiness(String email, Business business);
    
    @Query("SELECT cr FROM CollaborationRequest cr WHERE cr.token = :token AND cr.expiryDate > :now")
    Optional<CollaborationRequest> findValidTokenRequest(@Param("token") String token, @Param("now") LocalDateTime now);
    
    void deleteByExpiryDateBefore(LocalDateTime date);
}
