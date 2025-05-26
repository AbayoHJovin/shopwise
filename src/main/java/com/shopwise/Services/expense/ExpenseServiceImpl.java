package com.shopwise.Services.expense;

import com.shopwise.Dto.expense.ExpenseRequest;
import com.shopwise.Dto.expense.ExpenseResponse;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ExpenseRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import com.shopwise.models.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public ExpenseResponse createExpense(UUID businessId, ExpenseRequest request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> ExpenseException.notFound("Business not found with ID: " + businessId));
        
        // Create new expense
        Expense expense = new Expense();
        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setNote(request.getNote());
        
        // Set creation time (use provided time or current time)
        LocalDateTime createdAt = request.getCreatedAt() != null ? 
                request.getCreatedAt() : LocalDateTime.now();
        expense.setCreatedAt(createdAt);
        
        expense.setBusiness(business);
        
        // Save expense
        Expense savedExpense = expenseRepository.save(expense);
        
        // Log the expense creation in daily summary
        String formattedAmount = String.format("%.2f", savedExpense.getAmount());
        dailySummaryService.logDailyAction(businessId, 
                "Expense '" + savedExpense.getTitle() + "' of amount " + formattedAmount + 
                " in category '" + savedExpense.getCategory() + "' was recorded");
        
        // Return response
        return mapToExpenseResponse(savedExpense);
    }

    @Override
    @Transactional
    public List<ExpenseResponse> getExpensesByBusiness(UUID businessId) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw ExpenseException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all expenses for the business
        List<Expense> expenses = expenseRepository.findByBusinessId(businessId);
        
        // Map to response DTOs
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ExpenseResponse> getExpensesByDateRange(UUID businessId, LocalDate startDate, LocalDate endDate) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw ExpenseException.notFound("Business not found with ID: " + businessId);
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            throw ExpenseException.badRequest("Start date cannot be after end date");
        }
        
        // Convert LocalDate to LocalDateTime (start of day to end of day)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        // Get expenses within date range
        List<Expense> expenses = expenseRepository.findByBusinessIdAndDateRange(
                businessId, startDateTime, endDateTime);
        
        // Map to response DTOs
        return expenses.stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteExpense(Long expenseId) {
        // Check if expense exists
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> ExpenseException.notFound("Expense not found with ID: " + expenseId));
        
        // Get business ID before deleting the expense
        UUID businessId = expense.getBusiness().getId();
        String expenseTitle = expense.getTitle();
        String formattedAmount = String.format("%.2f", expense.getAmount());
        String category = expense.getCategory();
        
        // Delete expense
        expenseRepository.delete(expense);
        
        // Log the expense deletion in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Expense '" + expenseTitle + "' of amount " + formattedAmount + 
                " in category '" + category + "' was deleted");
    }
    
    // Helper method to map Expense entity to ExpenseResponse DTO
    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .note(expense.getNote())
                .createdAt(expense.getCreatedAt())
                .businessId(expense.getBusiness().getId())
                .businessName(expense.getBusiness().getName())
                .build();
    }
}
