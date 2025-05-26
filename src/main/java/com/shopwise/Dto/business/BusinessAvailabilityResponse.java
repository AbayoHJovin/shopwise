package com.shopwise.Dto.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for business availability status response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAvailabilityResponse {
    private UUID businessId;
    private String businessName;
    private boolean open;
}
