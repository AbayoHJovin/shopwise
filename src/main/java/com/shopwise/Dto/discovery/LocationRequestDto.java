package com.shopwise.Dto.discovery;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location-based requests containing user's current coordinates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDto {
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    private Double longitude;
    
    // Optional radius in kilometers for proximity searches
    private Double radius;
    
    // Pagination parameters
    @Builder.Default
    private Integer skip = 0;
    @Builder.Default
    private Integer limit = 10;
}
