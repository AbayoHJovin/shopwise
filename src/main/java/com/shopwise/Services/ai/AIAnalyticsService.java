package com.shopwise.Services.ai;

import com.shopwise.Repository.ExpenseRepository;
import com.shopwise.Repository.SaleRecordRepository;
import com.shopwise.models.Business;
import com.shopwise.models.Expense;
import com.shopwise.models.SaleRecord;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for generating AI analytics and summaries
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalyticsService {

    private final BusinessContextService businessContextService;
    private final GeminiService geminiService;
    private final ExpenseRepository expenseRepository;
    private final SaleRecordRepository saleRecordRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Generate a daily log summary for a business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the summary
     * @return A summary of the daily logs
     */
    @Transactional(readOnly = true)
    public String generateDailyLogSummary(UUID businessId, User user) {
        try {
            // Get today's date
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
            
            // Get business context
            String businessContext = businessContextService.getBusinessContext(businessId, user);
            
            // Create a Business reference with the ID
            Business businessRef = new Business();
            businessRef.setId(businessId);
            
            // Get today's sales
            List<SaleRecord> todaySales = saleRecordRepository.findByBusinessAndSaleTimeBetween(
                    businessRef, startOfDay, endOfDay);
            
            // Get today's expenses
            List<Expense> todayExpenses = expenseRepository.findByBusinessAndCreatedAtBetween(
                    businessRef, startOfDay, endOfDay);
            
            // Build the prompt for the AI
            StringBuilder prompt = new StringBuilder();
            prompt.append("I need a concise daily summary for a business based on the following data:\n\n");
            
            // Add today's sales
            prompt.append("TODAY'S SALES (").append(todaySales.size()).append(" sales):\n");
            if (!todaySales.isEmpty()) {
                for (SaleRecord sale : todaySales) {
                    prompt.append("- ").append(sale.getSaleTime().format(DATE_FORMATTER))
                           .append(": ").append(sale.getProduct().getName())
                           .append(", ").append(sale.getTotalPiecesSold()).append(" pieces\n");
                }
            } else {
                prompt.append("No sales recorded today.\n");
            }
            prompt.append("\n");
            
            // Add today's expenses
            prompt.append("TODAY'S EXPENSES (").append(todayExpenses.size()).append(" expenses):\n");
            if (!todayExpenses.isEmpty()) {
                for (Expense expense : todayExpenses) {
                    prompt.append("- ").append(expense.getCreatedAt().format(DATE_FORMATTER))
                           .append(": ").append(expense.getTitle())
                           .append(", $").append(formatNumber(expense.getAmount()))
                           .append(", Category: ").append(expense.getCategory()).append("\n");
                }
            } else {
                prompt.append("No expenses recorded today.\n");
            }
            prompt.append("\n");
            
            // Add business context for reference
            prompt.append("BUSINESS CONTEXT:\n");
            prompt.append(businessContext);
            
            // Add instructions for the AI
            prompt.append("\nBased on the above data, please provide a concise daily summary that includes:\n");
            prompt.append("1. A summary of today's sales and revenue\n");
            prompt.append("2. A summary of today's expenses\n");
            prompt.append("3. Net profit/loss for the day\n");
            prompt.append("4. Any notable trends or patterns\n");
            prompt.append("5. Brief recommendations for tomorrow\n\n");
            prompt.append("Format the summary in a professional, easy-to-read manner with clear sections.");
            
            // Set parameters for a concise, analytical response
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("temperature", 0.3);
            parameters.put("topK", 20);
            parameters.put("maxOutputTokens", 1024);
            
            // Get AI response
            Map<String, Object> response = geminiService.generateResponseWithMetadata(
                    prompt.toString(), null, parameters);
            
            return (String) response.get("content");
        } catch (Exception e) {
            log.error("Error generating daily log summary", e);
            return "Error: Unable to generate daily log summary. " + e.getMessage();
        }
    }
    
    /**
     * Generate expense analytics for a business
     * 
     * @param businessId The ID of the business
     * @param user The user requesting the analytics
     * @return An analysis of the business expenses
     */
    @Transactional(readOnly = true)
    public String generateExpenseAnalytics(UUID businessId, User user) {
        try {
            // Get business context
            String businessContext = businessContextService.getBusinessContext(businessId, user);
            
            // Get all expenses for the business
            List<Expense> allExpenses = expenseRepository.findByBusinessId(businessId);
            
            // Group expenses by category
            Map<String, Double> expensesByCategory = allExpenses.stream()
                    .collect(Collectors.groupingBy(
                            Expense::getCategory,
                            Collectors.summingDouble(Expense::getAmount)
                    ));
            
            // Build the prompt for the AI
            StringBuilder prompt = new StringBuilder();
            prompt.append("I need a detailed analysis of business expenses based on the following data:\n\n");
            
            // Add expense categories summary
            prompt.append("EXPENSE CATEGORIES SUMMARY:\n");
            for (Map.Entry<String, Double> entry : expensesByCategory.entrySet()) {
                prompt.append("- ").append(entry.getKey()).append(": $")
                       .append(formatNumber(entry.getValue())).append("\n");
            }
            prompt.append("\n");
            
            // Add detailed expense list
            prompt.append("DETAILED EXPENSES (").append(allExpenses.size()).append(" expenses):\n");
            if (!allExpenses.isEmpty()) {
                // Sort expenses by date (most recent first)
                allExpenses.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
                
                for (int i = 0; i < Math.min(20, allExpenses.size()); i++) {
                    Expense expense = allExpenses.get(i);
                    prompt.append("- ").append(expense.getCreatedAt().format(DATE_FORMATTER))
                           .append(": ").append(expense.getTitle())
                           .append(", $").append(formatNumber(expense.getAmount()))
                           .append(", Category: ").append(expense.getCategory()).append("\n");
                }
                if (allExpenses.size() > 20) {
                    prompt.append("- ... and ").append(allExpenses.size() - 20).append(" more expenses\n");
                }
            } else {
                prompt.append("No expenses recorded.\n");
            }
            prompt.append("\n");
            
            // Add business context for reference
            prompt.append("BUSINESS CONTEXT:\n");
            prompt.append(businessContext);
            
            // Add instructions for the AI
            prompt.append("\nBased on the above data, please provide a comprehensive expense analysis that includes:\n");
            prompt.append("1. A breakdown of expenses by category with percentages of total spend\n");
            prompt.append("2. Identification of the highest expense categories\n");
            prompt.append("3. Trends in spending over time (if discernible)\n");
            prompt.append("4. Recommendations for potential cost-saving opportunities\n");
            prompt.append("5. Suggestions for better expense management\n\n");
            prompt.append("Format the analysis in a professional, easy-to-read manner with clear sections and actionable insights.");
            
            // Set parameters for a detailed, analytical response
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("temperature", 0.2);
            parameters.put("topK", 20);
            parameters.put("maxOutputTokens", 2048);
            
            // Get AI response
            Map<String, Object> response = geminiService.generateResponseWithMetadata(
                    prompt.toString(), null, parameters);
            
            return (String) response.get("content");
        } catch (Exception e) {
            log.error("Error generating expense analytics", e);
            return "Error: Unable to generate expense analytics. " + e.getMessage();
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
