package com.shopwise.Services.dashboard;

import com.shopwise.Dto.dashboard.BusinessDashboardDto;
import com.shopwise.Dto.expense.ExpenseResponse;
import com.shopwise.Dto.product.ProductResponse;
import com.shopwise.models.User;

import java.util.List;
import java.util.UUID;

/**
 * Service for handling dashboard-related operations
 */
public interface DashboardService {
    
    /**
     * Get dashboard data for a business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the dashboard data
     * @return Dashboard data for the business
     * @throws SecurityException if the user doesn't have access to the business
     */
    BusinessDashboardDto getDashboardData(UUID businessId, User user);
    
    /**
     * Get top 5 best-selling products for a business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the data
     * @return List of top 5 best-selling products
     * @throws SecurityException if the user doesn't have access to the business
     */
    List<ProductResponse> getTopSellingProducts(UUID businessId, User user);
    
    /**
     * Get latest 5 expenses for a business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the data
     * @return List of latest 5 expenses
     * @throws SecurityException if the user doesn't have access to the business
     */
    List<ExpenseResponse> getLatestExpenses(UUID businessId, User user);
}
