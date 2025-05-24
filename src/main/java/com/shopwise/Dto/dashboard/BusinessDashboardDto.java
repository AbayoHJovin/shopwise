package com.shopwise.Dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for business dashboard data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDashboardDto {
    private UUID businessId;
    private String businessName;
    
    // Revenue data
    private BigDecimal totalRevenue;
    private BigDecimal previousMonthRevenue;
    private Double revenueChangePercentage;
    
    // Product data
    private int totalProducts;
    
    // Employee/Collaborator data
    private int totalEmployees;
    private int totalCollaborators;
    
    // Expense data
    private BigDecimal totalExpenses;
    private BigDecimal previousMonthExpenses;
    private Double expenseChangePercentage;
    
    // Stock investment
    private BigDecimal totalStockInvestment;
    
    // Monthly sales data for chart
    private List<MonthlySalesDto> monthlySales;
    private String highestSalesMonth;
}
