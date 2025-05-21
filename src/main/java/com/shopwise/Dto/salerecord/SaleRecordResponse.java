package com.shopwise.Dto.salerecord;

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
public class SaleRecordResponse {
    private UUID id;
    private int quantitySold;
    private LocalDateTime saleTime;
    private boolean manuallyAdjusted;
    private boolean loggedLater;
    private String notes;
    private LocalDateTime actualSaleTime;
    
    // Product details
    private UUID productId;
    private String productName;
    private double pricePerItem;
    private double totalSaleValue;
    
    // Business details
    private UUID businessId;
    private String businessName;
}
