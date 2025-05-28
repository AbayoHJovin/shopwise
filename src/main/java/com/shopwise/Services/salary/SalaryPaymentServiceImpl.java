package com.shopwise.Services.salary;

import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.Repository.SalaryPaymentRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import com.shopwise.models.Employee;
import com.shopwise.models.SalaryPayment;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of the SalaryPaymentService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryPaymentServiceImpl implements SalaryPaymentService {

    private final SalaryPaymentRepository salaryPaymentRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;

    /**
     * Create a new salary payment
     * 
     * @param employeeId The ID of the employee
     * @param amount The payment amount
     * @param paymentDate The payment date
     * @param currentUser The user making the payment
     * @return The created salary payment
     */
    @Override
    @Transactional
    public SalaryPayment createSalaryPayment(UUID employeeId, double amount, LocalDateTime paymentDate, User currentUser) {
        // Get the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        // Get the business
        Business business = employee.getBusiness();
        
        // Verify user has access to this business
        boolean isCollaborator = businessRepository.isUserCollaborator(business.getId(), currentUser.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this business");
        }
        
        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        
        // Create and save the salary payment
        SalaryPayment salaryPayment = new SalaryPayment();
        salaryPayment.setEmployee(employee);
        salaryPayment.setAmount(amount);
        salaryPayment.setPaymentDate(paymentDate != null ? paymentDate : LocalDateTime.now());
        
        // Save the salary payment
        SalaryPayment savedPayment = salaryPaymentRepository.save(salaryPayment);
        
        // Log the activity in the daily summary
        String description = String.format(
            "Salary payment of $%.2f made to %s (%s)", 
            amount, 
            employee.getName(), 
            employee.getRole().toString()
        );
        dailySummaryService.logDailyAction(business.getId(), description);
        
        return savedPayment;
    }

    /**
     * Get a salary payment by ID
     * 
     * @param paymentId The payment ID
     * @param currentUser The user requesting the payment
     * @return The salary payment
     */
    @Override
    @Transactional(readOnly = true)
    public SalaryPayment getSalaryPayment(UUID paymentId, User currentUser) {
        // Get the salary payment
        SalaryPayment salaryPayment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Salary payment not found"));
        
        // Get the business
        Business business = salaryPayment.getEmployee().getBusiness();
        
        // Verify user has access to this business
        boolean isCollaborator = businessRepository.isUserCollaborator(business.getId(), currentUser.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this salary payment");
        }
        
        return salaryPayment;
    }

    /**
     * Get all salary payments for an employee
     * 
     * @param employeeId The employee ID
     * @param currentUser The user requesting the payments
     * @return List of salary payments
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalaryPayment> getEmployeeSalaryPayments(UUID employeeId, User currentUser) {
        // Get the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        
        // Get the business
        Business business = employee.getBusiness();
        
        // Verify user has access to this business
        boolean isCollaborator = businessRepository.isUserCollaborator(business.getId(), currentUser.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this employee's salary payments");
        }
        
        return salaryPaymentRepository.findByEmployee(employee);
    }

    /**
     * Get all salary payments for a business
     * 
     * @param businessId The business ID
     * @param currentUser The user requesting the payments
     * @return List of salary payments
     */
    @Override
    @Transactional(readOnly = true)
    public List<SalaryPayment> getBusinessSalaryPayments(UUID businessId, User currentUser) {
        // Verify user has access to this business
        boolean isCollaborator = businessRepository.isUserCollaborator(businessId, currentUser.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to this business's salary payments");
        }
        
        return salaryPaymentRepository.findByBusinessId(businessId);
    }

    /**
     * Delete a salary payment
     * 
     * @param paymentId The payment ID
     * @param currentUser The user deleting the payment
     * @return true if deleted successfully
     */
    @Override
    @Transactional
    public boolean deleteSalaryPayment(UUID paymentId, User currentUser) {
        // Get the salary payment
        SalaryPayment salaryPayment = salaryPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Salary payment not found"));
        
        // Get the business
        Business business = salaryPayment.getEmployee().getBusiness();
        
        // Verify user has access to this business
        boolean isCollaborator = businessRepository.isUserCollaborator(business.getId(), currentUser.getId());
        if (!isCollaborator) {
            throw new SecurityException("User does not have access to delete this salary payment");
        }
        
        // Get employee and payment details before deletion for the log
        Employee employee = salaryPayment.getEmployee();
        double amount = salaryPayment.getAmount();
        
        // Delete the salary payment
        salaryPaymentRepository.delete(salaryPayment);
        
        // Log the deletion activity in the daily summary
        String description = String.format(
            "Salary payment of $%.2f to %s (%s) has been deleted", 
            amount, 
            employee.getName(), 
            employee.getRole().toString()
        );
        dailySummaryService.logDailyAction(business.getId(), description);
        
        return true;
    }
}
