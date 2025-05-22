package com.shopwise.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Location information with validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    
    @Size(max = 100, message = "Province name must not exceed 100 characters")
    private String province;
    
    @Size(max = 100, message = "District name must not exceed 100 characters")
    private String district;
    
    @Size(max = 100, message = "Sector name must not exceed 100 characters")
    private String sector;
    
    @Size(max = 100, message = "Cell name must not exceed 100 characters")
    private String cell;
    
    @Size(max = 100, message = "Village name must not exceed 100 characters")
    private String village;
    
    // Optional geographic coordinates with validation
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    private Double longitude;
    
    /**
     * Returns a formatted string representation of the location
     * @return Formatted location string
     */
    public String getFormattedLocation() {
        StringBuilder location = new StringBuilder();
        
        if (village != null && !village.isEmpty()) {
            location.append(village).append(", ");
        }
        
        if (cell != null && !cell.isEmpty()) {
            location.append(cell).append(", ");
        }
        
        if (sector != null && !sector.isEmpty()) {
            location.append(sector).append(", ");
        }
        
        if (district != null && !district.isEmpty()) {
            location.append(district).append(", ");
        }
        
        if (province != null && !province.isEmpty()) {
            location.append(province);
        }
        
        // Remove trailing comma and space if present
        String result = location.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        
        return result;
    }
}
