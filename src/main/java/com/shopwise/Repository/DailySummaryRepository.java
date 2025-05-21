package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, UUID> {
    
    List<DailySummary> findByBusiness(Business business);
    
    List<DailySummary> findByBusinessId(UUID businessId);
    
    @Query("SELECT ds FROM DailySummary ds WHERE ds.business.id = :businessId AND ds.timestamp BETWEEN :startDateTime AND :endDateTime ORDER BY ds.timestamp DESC")
    List<DailySummary> findByBusinessIdAndDateRange(
            @Param("businessId") UUID businessId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
