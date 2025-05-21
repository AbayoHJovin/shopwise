package com.shopwise.Dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {
    
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String password;
    
    @Min(value = 0, message = "Salary must be at least 0")
    private Double salary;
    
    private Boolean isCollaborator;
}
