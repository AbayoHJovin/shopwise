package com.shopwise.Dto.employee;

import com.shopwise.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String name;
    private String email;
    private double salary;
    private LocalDate joinedDate;
    private boolean isDisabled;
    private boolean isCollaborator;
    private boolean emailConfirmed;
    private Role role;
    private UUID businessId;
    private String businessName;
}
