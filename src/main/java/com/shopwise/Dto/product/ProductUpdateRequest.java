package com.shopwise.Dto.product;

import com.shopwise.Dto.productimage.ProductImageRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Min(value = 0, message = "Number of packets must be at least 0")
    private Integer packets;
    
    @Min(value = 1, message = "Items per packet must be at least 1")
    private Integer itemsPerPacket;
    
    @Min(value = 0, message = "Price per item must be at least 0")
    private Double pricePerItem;
    
    @Min(value = 0, message = "Fulfillment cost must be at least 0")
    private Double fulfillmentCost;
    
    // Optional product images (max 3)
    @Size(max = 3, message = "A product can have at most 3 images")
    private List<ProductImageRequest> images;
}
