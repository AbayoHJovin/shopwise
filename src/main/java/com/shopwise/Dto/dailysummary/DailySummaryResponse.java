package com.shopwise.Dto.dailysummary;

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
public class DailySummaryResponse {
    private UUID id;
    private String description;
    private LocalDateTime timestamp;
    
    // Business details
    private UUID businessId;
    private String businessName;
}
