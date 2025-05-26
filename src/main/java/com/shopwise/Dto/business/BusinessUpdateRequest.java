package com.shopwise.Dto.business;

import com.shopwise.Dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating business information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUpdateRequest {
    private String name;
    private LocationDto location;
    private String about;
    private String websiteLink;
}
