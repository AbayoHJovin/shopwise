package com.shopwise.Dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private int packets;
    private int itemsPerPacket;
    private double pricePerItem;
    private double fulfillmentCost;
    private UUID businessId;
    
    // Calculated fields
    private int totalItems;
    private double totalValue;
}
