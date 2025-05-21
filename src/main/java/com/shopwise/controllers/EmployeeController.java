package com.shopwise.controllers;

import com.shopwise.Dto.employee.CollaboratorInviteRequest;
import com.shopwise.Dto.employee.EmployeeRequest;
import com.shopwise.Dto.employee.EmployeeResponse;
import com.shopwise.Dto.employee.EmployeeUpdateRequest;
import com.shopwise.Services.employee.EmployeeException;
import com.shopwise.Services.employee.EmployeeService;
import com.shopwise.enums.Role;
import com.shopwise.models.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> addEmployee(@PathVariable UUID businessId,
                                        @Valid @RequestBody EmployeeRequest request,
                                        Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
            EmployeeResponse createdEmployee = employeeService.addEmployee(businessId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable UUID employeeId,
                                           @Valid @RequestBody EmployeeUpdateRequest request,
                                           Authentication authentication) {
        try {
            EmployeeResponse updatedEmployee = employeeService.updateEmployee(employeeId, request);
            return ResponseEntity.ok(updatedEmployee);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{employeeId}/disable")
    public ResponseEntity<?> disableEmployee(@PathVariable UUID employeeId,
                                            Authentication authentication) {
        try {
            employeeService.disableEmployee(employeeId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Employee disabled successfully");
            return ResponseEntity.ok(response);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getEmployeesByBusiness(@PathVariable UUID businessId,
                                                  Authentication authentication) {
        try {
            List<EmployeeResponse> employees = employeeService.getEmployeesByBusiness(businessId);
            return ResponseEntity.ok(employees);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{employeeId}/role")
    public ResponseEntity<?> assignRole(@PathVariable UUID employeeId,
                                       @RequestParam Role role,
                                       Authentication authentication) {
        try {
            employeeService.assignRole(employeeId, role);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Role assigned successfully");
            return ResponseEntity.ok(response);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/business/{businessId}/invite-collaborator")
    public ResponseEntity<?> inviteCollaborator(@PathVariable UUID businessId,
                                              @Valid @RequestBody CollaboratorInviteRequest request,
                                              Authentication authentication) {
        try {
            employeeService.inviteCollaborator(businessId, request);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Collaborator invitation sent successfully");
            return ResponseEntity.ok(response);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
