package com.shopwise.Dto.expense;

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
public class ExpenseResponse {
    private Long id;
    private String title;
    private double amount;
    private String category;
    private String note;
    private LocalDateTime createdAt;
    private UUID businessId;
    private String businessName;
}
