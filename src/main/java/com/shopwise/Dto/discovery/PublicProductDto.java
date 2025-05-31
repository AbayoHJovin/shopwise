package com.shopwise.Dto.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for public product information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProductDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private List<String> imageUrls;
    
    // Business information
    private UUID businessId;
    private String businessName;
    
    // Additional calculated fields for packet information
    private int fullPacketsAvailable;
    private int additionalUnits;
    private int itemsPerPacket;
    
    // Additional pricing details
    private BigDecimal unitPrice;
    private BigDecimal fulfillmentCost;
    private BigDecimal packetPrice; // Price per full packet
}
