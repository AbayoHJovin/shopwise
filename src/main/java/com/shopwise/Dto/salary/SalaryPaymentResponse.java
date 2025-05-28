package com.shopwise.Dto.salary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for salary payment response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPaymentResponse {
    
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private String employeeRole;
    private Double amount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paymentDate;
    
    private UUID businessId;
    private String businessName;
}
