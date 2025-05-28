package com.shopwise.Repository;

import com.shopwise.models.Employee;
import com.shopwise.models.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, UUID> {
    
    /**
     * Find all salary payments for a specific employee
     * 
     * @param employee The employee
     * @return List of salary payments
     */
    List<SalaryPayment> findByEmployee(Employee employee);
    
    /**
     * Find all salary payments for a specific employee within a date range
     * 
     * @param employee The employee
     * @param startDate The start date
     * @param endDate The end date
     * @return List of salary payments
     */
    List<SalaryPayment> findByEmployeeAndPaymentDateBetween(Employee employee, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all salary payments for employees in a specific business
     * 
     * @param businessId The business ID
     * @return List of salary payments
     */
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.business.id = :businessId")
    List<SalaryPayment> findByBusinessId(@Param("businessId") UUID businessId);
    
    /**
     * Find all salary payments for employees in a specific business within a date range
     * 
     * @param businessId The business ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of salary payments
     */
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.employee.business.id = :businessId AND sp.paymentDate BETWEEN :startDate AND :endDate")
    List<SalaryPayment> findByBusinessIdAndPaymentDateBetween(
            @Param("businessId") UUID businessId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
