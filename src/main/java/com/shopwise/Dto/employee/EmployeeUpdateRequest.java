package com.shopwise.Dto.employee;

import com.shopwise.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating employee information
 * Fields are optional but cannot be empty if provided
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {
    
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 0, message = "Salary must be at least 0")
    private Double salary;
    
    private Boolean isDisabled;
    
    private Boolean isCollaborator;
    
    private Role role;
}
