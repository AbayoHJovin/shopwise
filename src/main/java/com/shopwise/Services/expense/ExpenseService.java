package com.shopwise.Services.expense;

import com.shopwise.Dto.expense.ExpenseRequest;
import com.shopwise.Dto.expense.ExpenseResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing expenses in the ShopWise system.
 */
public interface ExpenseService {

    /**
     * Creates a new expense for a business.
     * @param businessId UUID of the business
     * @param request Expense details
     * @return Created ExpenseResponse
     */
    ExpenseResponse createExpense(UUID businessId, ExpenseRequest request);

    /**
     * Retrieves all expenses for a specific business.
     * @param businessId UUID of the business
     * @return List of ExpenseResponse objects
     */
    List<ExpenseResponse> getExpensesByBusiness(UUID businessId);

    /**
     * Retrieves expenses for a business within a date range.
     * @param businessId UUID of the business
     * @param startDate Start date of the range (inclusive)
     * @param endDate End date of the range (inclusive)
     * @return List of ExpenseResponse objects
     */
    List<ExpenseResponse> getExpensesByDateRange(UUID businessId, LocalDate startDate, LocalDate endDate);

    /**
     * Deletes an expense.
     * @param expenseId ID of the expense to delete
     */
    void deleteExpense(Long expenseId);
}
