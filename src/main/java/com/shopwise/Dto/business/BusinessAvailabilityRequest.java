package com.shopwise.Dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating business availability status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAvailabilityRequest {
    private boolean open;
}
