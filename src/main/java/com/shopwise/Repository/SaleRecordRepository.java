package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.Product;
import com.shopwise.models.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleRecordRepository extends JpaRepository<SaleRecord, UUID> {
    
    List<SaleRecord> findByProduct(Product product);
    
    List<SaleRecord> findByProductId(UUID productId);
    
    List<SaleRecord> findByBusiness(Business business);
    
    List<SaleRecord> findByBusinessId(UUID businessId);
    
    @Query("SELECT s FROM SaleRecord s WHERE s.business.id = :businessId AND s.saleTime BETWEEN :startDateTime AND :endDateTime")
    List<SaleRecord> findByBusinessIdAndDateRange(
            @Param("businessId") UUID businessId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Find sale records for a specific business within a date range
     * 
     * @param business The business to find sales for
     * @param startDateTime The start date and time
     * @param endDateTime The end date and time
     * @return List of sale records within the date range
     */
    List<SaleRecord> findByBusinessAndSaleTimeBetween(
            Business business,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime);
}
