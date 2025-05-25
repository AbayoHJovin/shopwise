package com.shopwise.Services.dashboard;

import com.shopwise.Dto.dashboard.BusinessDashboardDto;
import com.shopwise.Dto.dashboard.MonthlySalesDto;
import com.shopwise.Dto.expense.ExpenseResponse;
import com.shopwise.Dto.product.ProductResponse;
import com.shopwise.Dto.productimage.ProductImageResponse;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.Repository.ExpenseRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Repository.ProductImageRepository;
import com.shopwise.Repository.SaleRecordRepository;
import com.shopwise.models.Business;
import com.shopwise.models.Expense;
import com.shopwise.models.Product;
import com.shopwise.models.ProductImage;
import com.shopwise.models.SaleRecord;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Implementation of the DashboardService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final BusinessRepository businessRepository;
    private final SaleRecordRepository saleRecordRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Get dashboard data for a business
     *
     * @param businessId The ID of the business
     * @param user The user requesting the dashboard data
     * @return Dashboard data for the business
     * @throws SecurityException if the user doesn't have access to the business
     */
    @Override
    @Transactional(readOnly = true)
    public BusinessDashboardDto getDashboardData(UUID businessId, User user) {
        // Verify business exists and user has access
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
        
        // Check if user is a collaborator
        boolean isCollaborator = businessRepository.isUserCollaborator(businessId, user.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this business");
        }
        
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();
        
        // Calculate previous month
        YearMonth previousMonth = YearMonth.of(currentYear, currentMonth).minusMonths(1);
        
        // Build the dashboard DTO
        BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder = BusinessDashboardDto.builder()
                .businessId(business.getId())
                .businessName(business.getName());
        
        // Calculate revenue metrics
        calculateRevenueMetrics(business, dashboardBuilder, currentYear, currentMonth, previousMonth);
        
        // Calculate product metrics
        calculateProductMetrics(business, dashboardBuilder);
        
        // Calculate employee and collaborator metrics
        calculatePeopleMetrics(business, dashboardBuilder);
        
        // Calculate expense metrics
        calculateExpenseMetrics(business, dashboardBuilder, currentYear, currentMonth, previousMonth);
        
        // Calculate stock investment
        calculateStockInvestment(business, dashboardBuilder);
        
        // Calculate monthly sales for chart
        List<MonthlySalesDto> monthlySales = calculateMonthlySales(business, currentYear);
        dashboardBuilder.monthlySales(monthlySales);
        
        // Find month with highest sales
        Optional<MonthlySalesDto> highestSalesMonth = monthlySales.stream()
                .max(Comparator.comparing(MonthlySalesDto::getRevenue));
        
        highestSalesMonth.ifPresent(month -> {
            dashboardBuilder.highestSalesMonth(month.getMonth());
            // Mark the highest month in the list
            monthlySales.forEach(m -> m.setHighest(m.getMonth().equals(month.getMonth())));
        });
        
        // Get top selling products
        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> topProductsData = saleRecordRepository.findTopSellingProductsByBusinessId(
                businessId, pageable);
        
        // Convert to ProductResponse DTOs
        List<ProductResponse> topProducts = new ArrayList<>();
        
        for (Object[] data : topProductsData) {
            Product product = (Product) data[0];
            Long totalSold = ((Number) data[1]).longValue();
            
            // Get product images
            List<ProductImage> productImages = productImageRepository.findByProductId(product.getId());
            List<ProductImageResponse> imageResponses = productImages.stream()
                    .map(image -> ProductImageResponse.builder()
                            .id(image.getId())
                            .imageUrl(image.getImageUrl())
                            .publicId(image.getPublicId())
                            .productId(product.getId())
                            .build())
                    .collect(Collectors.toList());
            
            // Calculate total items and value
            int totalItems = product.getPackets() * product.getItemsPerPacket();
            double totalValue = product.getPricePerItem() * totalItems;
            
            // Build product response
            ProductResponse productResponse = ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .packets(product.getPackets())
                    .itemsPerPacket(product.getItemsPerPacket())
                    .pricePerItem(product.getPricePerItem())
                    .fulfillmentCost(product.getFulfillmentCost())
                    .businessId(product.getBusiness().getId())
                    .totalItems(totalItems)
                    .totalValue(totalValue)
                    .images(imageResponses)
                    .build();
            
            topProducts.add(productResponse);
        }
        
        // Get latest expenses
        List<Expense> latestExpenses = expenseRepository.findByBusinessOrderByCreatedAtDesc(
                business, pageable);
        
        // Convert to ExpenseResponse DTOs
        List<ExpenseResponse> latestExpenseResponses = latestExpenses.stream()
                .map(expense -> ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .category(expense.getCategory())
                        .note(expense.getNote())
                        .createdAt(expense.getCreatedAt())
                        .businessId(expense.getBusiness().getId())
                        .businessName(expense.getBusiness().getName())
                        .build())
                .collect(Collectors.toList());
        
        // Add top products and latest expenses to the dashboard
        dashboardBuilder
                .topSellingProducts(topProducts)
                .latestExpenses(latestExpenseResponses);
        
        return dashboardBuilder.build();
    }
    
    /**
     * Calculate revenue metrics for the dashboard
     */
    private void calculateRevenueMetrics(Business business, BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder,
                                        int currentYear, int currentMonth, YearMonth previousMonth) {
        // Get all sale records for the current month
        LocalDateTime currentMonthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0);
        LocalDateTime currentMonthEnd = currentMonthStart.plusMonths(1).minusNanos(1);
        
        List<SaleRecord> currentMonthSales = saleRecordRepository.findByBusinessAndSaleTimeBetween(
                business, currentMonthStart, currentMonthEnd);
        
        // Calculate total revenue for current month
        BigDecimal totalRevenue = calculateTotalRevenue(currentMonthSales);
        
        // Get all sale records for the previous month
        LocalDateTime previousMonthStart = LocalDateTime.of(previousMonth.getYear(), previousMonth.getMonthValue(), 1, 0, 0);
        LocalDateTime previousMonthEnd = previousMonthStart.plusMonths(1).minusNanos(1);
        
        List<SaleRecord> previousMonthSales = saleRecordRepository.findByBusinessAndSaleTimeBetween(
                business, previousMonthStart, previousMonthEnd);
        
        // Calculate total revenue for previous month
        BigDecimal previousMonthRevenue = calculateTotalRevenue(previousMonthSales);
        
        // Calculate percentage change
        Double revenueChangePercentage = calculatePercentageChange(previousMonthRevenue, totalRevenue);
        
        // Set values in the dashboard builder
        dashboardBuilder
                .totalRevenue(totalRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .revenueChangePercentage(revenueChangePercentage);
    }
    
    /**
     * Calculate product metrics for the dashboard
     */
    private void calculateProductMetrics(Business business, BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder) {
        // Get total number of products
        int totalProducts = business.getProducts() != null ? business.getProducts().size() : 0;
        
        // Set values in the dashboard builder
        dashboardBuilder.totalProducts(totalProducts);
    }
    
    /**
     * Calculate employee and collaborator metrics for the dashboard
     */
    private void calculatePeopleMetrics(Business business, BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder) {
        // Get total number of employees
        int totalEmployees = business.getEmployees() != null ? business.getEmployees().size() : 0;
        
        // Get total number of collaborators
        int totalCollaborators = business.getCollaborators() != null ? business.getCollaborators().size() : 0;
        
        // Set values in the dashboard builder
        dashboardBuilder
                .totalEmployees(totalEmployees)
                .totalCollaborators(totalCollaborators);
    }
    
    /**
     * Calculate expense metrics for the dashboard
     */
    private void calculateExpenseMetrics(Business business, BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder,
                                        int currentYear, int currentMonth, YearMonth previousMonth) {
        // Get all expenses for the current month
        LocalDateTime currentMonthStart = LocalDateTime.of(currentYear, currentMonth, 1, 0, 0);
        LocalDateTime currentMonthEnd = currentMonthStart.plusMonths(1).minusNanos(1);
        
        List<Expense> currentMonthExpenses = expenseRepository.findByBusinessAndCreatedAtBetween(
                business, currentMonthStart, currentMonthEnd);
        
        // Calculate total expenses for current month
        BigDecimal totalExpenses = calculateTotalExpenses(currentMonthExpenses);
        
        // Get all expenses for the previous month
        LocalDateTime previousMonthStart = LocalDateTime.of(previousMonth.getYear(), previousMonth.getMonthValue(), 1, 0, 0);
    }
    
    /**
     * Calculate stock investment for the dashboard
     * 
     * @param business The business to calculate stock investment for
     * @param dashboardBuilder The dashboard builder to update with the calculated value
     */
    private void calculateStockInvestment(Business business, BusinessDashboardDto.BusinessDashboardDtoBuilder dashboardBuilder) {
        // Get all products for the business
        List<Product> products = business.getProducts();
        
        // Calculate total stock investment
        BigDecimal totalStockInvestment = BigDecimal.ZERO;
        
        if (products != null) {
            for (Product product : products) {
                BigDecimal productValue = BigDecimal.valueOf(product.getPricePerItem())
                        .multiply(BigDecimal.valueOf((long) product.getPackets() * product.getItemsPerPacket()));
                totalStockInvestment = totalStockInvestment.add(productValue);
            }
        }
        
        // Set value in the dashboard builder
        dashboardBuilder.totalStockInvestment(totalStockInvestment);
    }
    
    /**
     * Calculate monthly sales for the chart
     */
    private List<MonthlySalesDto> calculateMonthlySales(Business business, int currentYear) {
        // Create a list to hold monthly sales data
        List<MonthlySalesDto> monthlySales = new ArrayList<>();
        
        // For each month of the current year
        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(currentYear, month, 1, 0, 0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusNanos(1);
            
            // Get all sale records for the month
            List<SaleRecord> monthSales = saleRecordRepository.findByBusinessAndSaleTimeBetween(
                    business, monthStart, monthEnd);
            
            // Calculate total revenue for the month
            BigDecimal monthRevenue = calculateTotalRevenue(monthSales);
            
            // Get month name
            String monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault());
            
            // Create monthly sales DTO
            MonthlySalesDto monthlySalesDto = MonthlySalesDto.builder()
                    .month(monthName)
                    .revenue(monthRevenue)
                    .salesCount(monthSales.size())
                    .isHighest(false) // Will be set later
                    .build();
            
            monthlySales.add(monthlySalesDto);
        }
        
        return monthlySales;
    }
    
    /**
     * Calculate total revenue from a list of sale records
     */
    private BigDecimal calculateTotalRevenue(List<SaleRecord> saleRecords) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        
        if (saleRecords != null) {
            for (SaleRecord saleRecord : saleRecords) {
                Product product = saleRecord.getProduct();
                if (product != null) {
                    BigDecimal saleValue = BigDecimal.valueOf(product.getPricePerItem())
                            .multiply(BigDecimal.valueOf(saleRecord.getQuantitySold()));
                    totalRevenue = totalRevenue.add(saleValue);
                }
            }
        }
        
        return totalRevenue;
    }
    
    /**
     * Calculate total expenses from a list of expenses
     */
    private BigDecimal calculateTotalExpenses(List<Expense> expenses) {
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        if (expenses != null) {
            for (Expense expense : expenses) {
                totalExpenses = totalExpenses.add(BigDecimal.valueOf(expense.getAmount()));
            }
        }
        
        return totalExpenses;
    }
    
    /**
     * Calculate percentage change between two values
     */
    private Double calculatePercentageChange(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            // If old value is zero, return 100% increase if new value is positive
            return newValue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        
        return newValue.subtract(oldValue)
                .divide(oldValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    /**
     * Get top 5 best-selling products for a business
     *
     * @param businessId The ID of the business
     * @param user The user requesting the data
     * @return List of top 5 best-selling products
     * @throws SecurityException if the user doesn't have access to the business
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopSellingProducts(UUID businessId, User user) {
        // Verify business exists and user has access
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
        
        // Check if user is a collaborator
        boolean isCollaborator = businessRepository.isUserCollaborator(businessId, user.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this business");
        }
        
        // Get top 5 selling products
        Pageable pageable = PageRequest.of(0, 5);
        List<Object[]> topProductsData = saleRecordRepository.findTopSellingProductsByBusinessId(
                businessId, pageable);
        
        // Convert to ProductResponse DTOs
        List<ProductResponse> topProducts = new ArrayList<>();
        
        for (Object[] data : topProductsData) {
            Product product = (Product) data[0];
            Long totalSold = ((Number) data[1]).longValue();
            
            // Get product images
            List<ProductImage> productImages = productImageRepository.findByProductId(product.getId());
            List<ProductImageResponse> imageResponses = productImages.stream()
                    .map(image -> ProductImageResponse.builder()
                            .id(image.getId())
                            .imageUrl(image.getImageUrl())
                            .publicId(image.getPublicId())
                            .productId(product.getId())
                            .build())
                    .collect(Collectors.toList());
            
            // Calculate total items and value
            int totalItems = product.getPackets() * product.getItemsPerPacket();
            double totalValue = product.getPricePerItem() * totalItems;
            
            // Build product response
            ProductResponse productResponse = ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .packets(product.getPackets())
                    .itemsPerPacket(product.getItemsPerPacket())
                    .pricePerItem(product.getPricePerItem())
                    .fulfillmentCost(product.getFulfillmentCost())
                    .businessId(product.getBusiness().getId())
                    .totalItems(totalItems)
                    .totalValue(totalValue)
                    .images(imageResponses)
                    .build();
            
            topProducts.add(productResponse);
        }
        
        return topProducts;
    }
    
    /**
     * Get latest 5 expenses for a business
     *
     * @param businessId The ID of the business
     * @param user The user requesting the data
     * @return List of latest 5 expenses
     * @throws SecurityException if the user doesn't have access to the business
     */
    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getLatestExpenses(UUID businessId, User user) {
        // Verify business exists and user has access
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));
        
        // Check if user is a collaborator
        boolean isCollaborator = businessRepository.isUserCollaborator(businessId, user.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this business");
        }
        
        // Get latest 5 expenses
        Pageable pageable = PageRequest.of(0, 5);
        List<Expense> latestExpenses = expenseRepository.findByBusinessOrderByCreatedAtDesc(
                business, pageable);
        
        // Convert to ExpenseResponse DTOs
        return latestExpenses.stream()
                .map(expense -> ExpenseResponse.builder()
                        .id(expense.getId())
                        .title(expense.getTitle())
                        .amount(expense.getAmount())
                        .category(expense.getCategory())
                        .note(expense.getNote())
                        .createdAt(expense.getCreatedAt())
                        .businessId(expense.getBusiness().getId())
                        .businessName(expense.getBusiness().getName())
                        .build())
                .collect(Collectors.toList());
    }
}
