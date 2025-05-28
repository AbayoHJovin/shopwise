package com.shopwise.Services.salary;

import com.shopwise.models.SalaryPayment;
import com.shopwise.models.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing salary payments
 */
public interface SalaryPaymentService {
    
    /**
     * Create a new salary payment
     * 
     * @param employeeId The ID of the employee
     * @param amount The payment amount
     * @param paymentDate The payment date
     * @param currentUser The user making the payment
     * @return The created salary payment
     */
    SalaryPayment createSalaryPayment(UUID employeeId, double amount, LocalDateTime paymentDate, User currentUser);
    
    /**
     * Get a salary payment by ID
     * 
     * @param paymentId The payment ID
     * @param currentUser The user requesting the payment
     * @return The salary payment
     */
    SalaryPayment getSalaryPayment(UUID paymentId, User currentUser);
    
    /**
     * Get all salary payments for an employee
     * 
     * @param employeeId The employee ID
     * @param currentUser The user requesting the payments
     * @return List of salary payments
     */
    List<SalaryPayment> getEmployeeSalaryPayments(UUID employeeId, User currentUser);
    
    /**
     * Get all salary payments for a business
     * 
     * @param businessId The business ID
     * @param currentUser The user requesting the payments
     * @return List of salary payments
     */
    List<SalaryPayment> getBusinessSalaryPayments(UUID businessId, User currentUser);
    
    /**
     * Delete a salary payment
     * 
     * @param paymentId The payment ID
     * @param currentUser The user deleting the payment
     * @return true if deleted successfully
     */
    boolean deleteSalaryPayment(UUID paymentId, User currentUser);
}
