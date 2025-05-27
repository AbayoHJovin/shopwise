package com.shopwise.Services.ai;

import com.shopwise.Dto.dashboard.BusinessDashboardDto;
import com.shopwise.Repository.*;
import com.shopwise.Services.dashboard.DashboardService;
import com.shopwise.models.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to provide business context data to the AI assistant
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessContextService {

    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final ExpenseRepository expenseRepository;
    private final DashboardService dashboardService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Get context information about a specific business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the context
     * @return A string containing business context information
     */
    public String getBusinessContext(UUID businessId, User user) {
        try {
            StringBuilder context = new StringBuilder();
            context.append("BUSINESS CONTEXT INFORMATION:\n\n");
            
            // Get basic business information
            Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
            
            // Verify user has access to this business
            boolean isCollaborator = businessRepository.isUserCollaborator(businessId, user.getId());
            if (!isCollaborator) {
                throw new SecurityException("User does not have access to this business");
            }
            
            // Add business details
            context.append("Business Name: ").append(business.getName()).append("\n");
            context.append("Location: ").append(business.getLocation()).append("\n");
            if (business.getAbout() != null && !business.getAbout().isEmpty()) {
                context.append("About: ").append(business.getAbout()).append("\n");
            }
            context.append("\n");
            
            // Add dashboard summary
            try {
                BusinessDashboardDto dashboard = dashboardService.getDashboardData(businessId, user);
                context.append("DASHBOARD SUMMARY:\n");
                context.append("Total Revenue: $").append(formatNumber(dashboard.getTotalRevenue())).append("\n");
                context.append("Total Products: ").append(dashboard.getTotalProducts()).append("\n");
                context.append("Total Employees: ").append(dashboard.getTotalEmployees()).append("\n");
                context.append("Total Collaborators: ").append(dashboard.getTotalCollaborators()).append("\n");
                
                if (dashboard.getTotalExpenses() != null) {
                    context.append("Total Expenses: $").append(formatNumber(dashboard.getTotalExpenses())).append("\n");
                }
                
                context.append("Total Stock Investment: $").append(formatNumber(dashboard.getTotalStockInvestment())).append("\n");
                context.append("Highest Sales Month: ").append(dashboard.getHighestSalesMonth()).append("\n\n");
            } catch (Exception e) {
                log.error("Error getting dashboard data", e);
                context.append("Dashboard data not available.\n\n");
            }
            
            // Add product summary
            List<Product> products = productRepository.findByBusinessId(businessId);
            context.append("PRODUCT SUMMARY (").append(products.size()).append(" products):\n");
            if (!products.isEmpty()) {
                for (int i = 0; i < Math.min(5, products.size()); i++) {
                    Product product = products.get(i);
                    context.append("- ").append(product.getName())
                           .append(": $").append(formatNumber(product.getPricePerItem()))
                           .append(" (").append(product.getPackets()).append(" packets, ")
                           .append(product.getItemsPerPacket()).append(" items per packet)\n");
                }
                if (products.size() > 5) {
                    context.append("- ... and ").append(products.size() - 5).append(" more products\n");
                }
            } else {
                context.append("No products available.\n");
            }
            context.append("\n");
            
            // Add employee summary
            List<Employee> employees = employeeRepository.findByBusiness_Id(businessId);
            context.append("EMPLOYEE SUMMARY (").append(employees.size()).append(" employees):\n");
            if (!employees.isEmpty()) {
                for (int i = 0; i < Math.min(5, employees.size()); i++) {
                    Employee employee = employees.get(i);
                    context.append("- ").append(employee.getName())
                           .append(", ").append(employee.getRole())
                           .append(", $").append(formatNumber(employee.getSalary())).append("/month\n");
                }
                if (employees.size() > 5) {
                    context.append("- ... and ").append(employees.size() - 5).append(" more employees\n");
                }
            } else {
                context.append("No employees available.\n");
            }
            context.append("\n");
            
            // Add recent sales
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            List<SaleRecord> recentSales = saleRecordRepository.findByBusinessAndSaleTimeBetween(
                    business, oneMonthAgo, LocalDateTime.now());
            // Sort the sales by date (most recent first)
            recentSales.sort((s1, s2) -> s2.getSaleTime().compareTo(s1.getSaleTime()));
            
            context.append("RECENT SALES (last 30 days, ").append(recentSales.size()).append(" sales):\n");
            if (!recentSales.isEmpty()) {
                for (int i = 0; i < Math.min(5, recentSales.size()); i++) {
                    SaleRecord sale = recentSales.get(i);
                    context.append("- ").append(sale.getSaleTime().format(DATE_FORMATTER))
                           .append(": ").append(sale.getProduct().getName())
                           .append(", ").append(sale.getTotalPiecesSold()).append(" pieces\n");
                }
                if (recentSales.size() > 5) {
                    context.append("- ... and ").append(recentSales.size() - 5).append(" more sales\n");
                }
            } else {
                context.append("No recent sales.\n");
            }
            context.append("\n");
            
            // Add recent expenses
            List<Expense> recentExpenses = expenseRepository.findByBusinessAndCreatedAtBetween(
                    business, oneMonthAgo, LocalDateTime.now());
            // Sort the expenses by date (most recent first)
            recentExpenses.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
            
            context.append("RECENT EXPENSES (last 30 days, ").append(recentExpenses.size()).append(" expenses):\n");
            if (!recentExpenses.isEmpty()) {
                for (int i = 0; i < Math.min(5, recentExpenses.size()); i++) {
                    Expense expense = recentExpenses.get(i);
                    context.append("- ").append(expense.getCreatedAt().format(DATE_FORMATTER))
                           .append(": ").append(expense.getTitle())
                           .append(", $").append(formatNumber(expense.getAmount()))
                           .append(", Category: ").append(expense.getCategory()).append("\n");
                }
                if (recentExpenses.size() > 5) {
                    context.append("- ... and ").append(recentExpenses.size() - 5).append(" more expenses\n");
                }
            } else {
                context.append("No recent expenses.\n");
            }
            
            return context.toString();
        } catch (Exception e) {
            log.error("Error getting business context", e);
            return "Error: Unable to retrieve business context information.";
        }
    }
    
    /**
     * Format a number for display
     * 
     * @param number The number to format
     * @return The formatted number
     */
    private String formatNumber(Number number) {
        if (number == null) {
            return "0";
        }
        return String.format("%,.2f", number.doubleValue());
    }
}
