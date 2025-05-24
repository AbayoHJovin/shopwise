package com.shopwise.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly sales data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySalesDto {
    private String month;
    private BigDecimal revenue;
    private int salesCount;
    private boolean isHighest;
}
