package com.shopwise.Dto.discovery;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product pagination and sorting requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageRequestDto {
    
    /**
     * Number of items to skip (for pagination)
     */
    @Builder.Default
    @Min(value = 0, message = "Skip value must be non-negative")
    private Integer skip = 0;
    
    /**
     * Maximum number of items to return
     */
    @Builder.Default
    @Min(value = 1, message = "Limit must be at least 1")
    private Integer limit = 10;
    
    /**
     * Field to sort by (name, price, etc.)
     */
    @Builder.Default
    private String sortBy = "name";
    
    /**
     * Sort direction (asc or desc)
     */
    @Builder.Default
    private String sortDirection = "asc";
    
    /**
     * Optional search term to filter products by name
     */
    private String searchTerm;
}
