package com.shopwise.Dto.discovery;

import com.shopwise.Dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for business discovery results with distance information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDiscoveryDto {
    private UUID id;
    private String name;
    private LocationDto location;
    private String about;
    private String websiteLink;
    private int productCount;
    
    // Distance from user in kilometers
    private Double distanceKm;
    
    private String formattedDistance;
}
