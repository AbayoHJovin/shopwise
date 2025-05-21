package com.shopwise.Dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Number of packets is required")
    @Min(value = 0, message = "Number of packets must be at least 0")
    private Integer packets;
    
    @NotNull(message = "Items per packet is required")
    @Min(value = 1, message = "Items per packet must be at least 1")
    private Integer itemsPerPacket;
    
    @NotNull(message = "Price per item is required")
    @Min(value = 0, message = "Price per item must be at least 0")
    private Double pricePerItem;
    
    @NotNull(message = "Fulfillment cost is required")
    @Min(value = 0, message = "Fulfillment cost must be at least 0")
    private Double fulfillmentCost;
}
