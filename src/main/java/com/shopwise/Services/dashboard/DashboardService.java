package com.shopwise.Services.dashboard;

import com.shopwise.Dto.dashboard.BusinessDashboardDto;
import com.shopwise.models.User;

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
}
