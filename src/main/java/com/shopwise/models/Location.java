package com.shopwise.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable Location class for storing detailed location information
 * including administrative divisions and optional geographic coordinates.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    private String province;
    private String district;
    private String sector;
    private String cell;
    private String village;
    
    private Double latitude;
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
    
    public String getCoordinates() {
        if (latitude != null && longitude != null) {
            return latitude + ", " + longitude;
        }
        return null;
    }
}
