package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    
    @Query("SELECT b FROM Business b WHERE :user MEMBER OF b.collaborators")
    List<Business> findBusinessesByCollaborator(@Param("user") User user);
    
    /**
     * Check if a user is a collaborator of a business
     * 
     * @param businessId The business ID
     * @param userId The user ID
     * @return true if the user is a collaborator, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b JOIN b.collaborators c WHERE b.id = :businessId AND c.id = :userId")
    boolean isUserCollaborator(@Param("businessId") UUID businessId, @Param("userId") UUID userId);
    
    Optional<Business> findById(UUID id);

}
