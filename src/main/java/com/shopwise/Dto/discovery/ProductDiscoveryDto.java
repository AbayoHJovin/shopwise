package com.shopwise.Dto.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for product discovery results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscoveryDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private UUID businessId;
    private String businessName;
    private List<String> imageUrls;
    
    // Distance from user in kilometers (if applicable)
    private Double distanceKm;
    
    // Formatted distance for display (e.g., "2.5 km")
    private String formattedDistance;
    
    // Additional calculated fields for packet information
    private Integer fullPacketsAvailable;
    private Integer additionalUnits;
    private Integer itemsPerPacket;
    
    // Additional pricing details
    private BigDecimal unitPrice;
    private BigDecimal fulfillmentCost;
    private BigDecimal packetPrice; // Price per full packet
}
