package com.shopwise.Dto.salerecord;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class SaleRecordRequest {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Quantity sold is required")
    @Min(value = 1, message = "Quantity sold must be at least 1")
    private Integer quantitySold;
    
    private LocalDateTime saleTime; // If not provided, current time will be used
    
    private boolean manuallyAdjusted;
    
    private boolean loggedLater;
    
    private String notes;
    
    private LocalDateTime actualSaleTime; // Used when loggedLater is true
}
