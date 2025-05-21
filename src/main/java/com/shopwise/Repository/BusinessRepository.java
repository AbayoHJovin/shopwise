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
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b WHERE b.id = :businessId AND :user MEMBER OF b.collaborators")
    boolean isUserCollaborator(@Param("businessId") UUID businessId, @Param("user") User user);
    
    Optional<Business> findById(UUID id);
}
