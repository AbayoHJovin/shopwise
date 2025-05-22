package com.shopwise.Dto.availability;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilitySlotResponse {
    private UUID id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    
    // Business details
    private UUID businessId;
    private String businessName;
}
