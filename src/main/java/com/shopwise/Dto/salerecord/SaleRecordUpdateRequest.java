package com.shopwise.Dto.salerecord;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRecordUpdateRequest {
    
    private UUID productId;
    
    @Min(value = 1, message = "Quantity sold must be at least 1")
    private Integer quantitySold;
    
    private LocalDateTime saleTime;
    
    private Boolean manuallyAdjusted;
    
    private Boolean loggedLater;
    
    private String notes;
    
    private LocalDateTime actualSaleTime;
}
