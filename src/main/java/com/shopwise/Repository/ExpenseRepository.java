package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    
    List<Expense> findByBusiness(Business business);
    
    List<Expense> findByBusinessId(UUID businessId);
    
    @Query("SELECT e FROM Expense e WHERE e.business.id = :businessId AND e.createdAt BETWEEN :startDate AND :endDate")
    List<Expense> findByBusinessIdAndDateRange(
            @Param("businessId") UUID businessId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find expenses for a specific business within a date range
     * 
     * @param business The business to find expenses for
     * @param startDate The start date and time
     * @param endDate The end date and time
     * @return List of expenses within the date range
     */
    List<Expense> findByBusinessAndCreatedAtBetween(
            Business business,
            LocalDateTime startDate,
            LocalDateTime endDate);
}
