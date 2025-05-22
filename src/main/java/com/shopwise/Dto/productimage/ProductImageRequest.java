package com.shopwise.Dto.productimage;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    @NotBlank(message = "Public ID is required")
    private String publicId;
}
