package com.shopwise.Repository;

import com.shopwise.models.AvailabilitySlot;
import com.shopwise.models.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    
    List<AvailabilitySlot> findByBusiness(Business business);
    
    List<AvailabilitySlot> findByBusinessId(UUID businessId);
    
    @Query("SELECT a FROM AvailabilitySlot a WHERE a.business.id = :businessId AND " +
           "((a.startTime <= :endTime AND a.endTime >= :startTime) OR " +
           "(a.startTime >= :startTime AND a.startTime <= :endTime))")
    List<AvailabilitySlot> findOverlappingSlots(
            @Param("businessId") UUID businessId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM AvailabilitySlot a WHERE a.business.id = :businessId AND " +
           "a.startTime >= :startTime ORDER BY a.startTime ASC")
    List<AvailabilitySlot> findUpcomingSlots(
            @Param("businessId") UUID businessId,
            @Param("startTime") LocalDateTime startTime);
}
