package com.shopwise.controllers;

import com.shopwise.Dto.employee.CollaboratorInviteRequest;
import com.shopwise.Dto.employee.EmployeeRequest;
import com.shopwise.Dto.employee.EmployeeResponse;
import com.shopwise.Dto.employee.EmployeeUpdateRequest;
import com.shopwise.Services.employee.EmployeeException;
import com.shopwise.Services.employee.EmployeeService;
import com.shopwise.enums.Role;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    
    /**
     * Get an employee by ID
     * 
     * @param employeeId ID of the employee to retrieve
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return The employee details if found and authorized
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable UUID employeeId,
                                           HttpServletRequest httpRequest,
                                           Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract business ID from cookie
            String businessIdStr = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected"));
            }
            
            UUID businessId = UUID.fromString(businessIdStr);
            
            // Check if user is authorized for this business
            if (!employeeService.isUserAuthorizedForBusiness(businessId, currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not authorized to access this business"));
            }
            
            // Get employee by ID and verify business ownership
            EmployeeResponse employee = employeeService.getEmployeeByIdAndBusiness(employeeId, businessId);
            
            return ResponseEntity.ok(employee);
            
        } catch (EmployeeException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid UUID format"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Add a new employee to the selected business
     * 
     * @param request Employee details
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return The created employee
     */
    @PostMapping("/add")
    public ResponseEntity<?> addEmployee(@Valid @RequestBody EmployeeRequest request,
                                        HttpServletRequest httpRequest,
                                        Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the selected business ID from cookies
            Cookie[] cookies = httpRequest.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                // Convert string to UUID
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Verify that the user has access to the business
                boolean hasAccess = employeeService.isUserAuthorizedForBusiness(businessId, currentUser.getId());
                if (!hasAccess) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You don't have permission to add employees to this business"));
                }
                
                // Add the employee
                EmployeeResponse createdEmployee = employeeService.addEmployee(businessId, request);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
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

    /**
     * Update an employee's information
     * 
     * @param employeeId ID of the employee to update
     * @param request The update request containing the fields to update
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return The updated employee details
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updateEmployee(@PathVariable UUID employeeId,
                                           @Valid @RequestBody EmployeeUpdateRequest request,
                                           HttpServletRequest httpRequest,
                                           Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Find the employee to get their business ID
            UUID businessId = employeeService.getEmployeeBusinessId(employeeId);
            if (businessId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Employee not found or has no associated business"));
            }
            
            // Verify that the user is the owner of the business
            boolean isOwner = employeeService.isUserBusinessOwner(businessId, currentUser.getId());
            if (!isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only the business owner can update employee information"));
            }
            
            // Update the employee
            EmployeeResponse updatedEmployee = employeeService.updateEmployee(employeeId, request, currentUser);
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

    /**
     * Get all employees for the selected business
     * 
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return List of employees for the business
     */
    @GetMapping("/get-by-business")
    public ResponseEntity<?> getEmployeesByBusiness(
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the selected business ID from cookies
            Cookie[] cookies = httpRequest.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                // Convert string to UUID
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Verify that the user has access to the business
                boolean hasAccess = employeeService.isUserAuthorizedForBusiness(businessId, currentUser.getId());
                if (!hasAccess) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You don't have permission to view employees of this business"));
                }
                
                // Get employees for the business
                List<EmployeeResponse> employees = employeeService.getEmployeesByBusiness(businessId);
                return ResponseEntity.ok(employees);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
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
