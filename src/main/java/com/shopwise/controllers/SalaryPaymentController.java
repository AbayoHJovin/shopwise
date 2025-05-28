package com.shopwise.controllers;

import com.shopwise.Dto.ApiResponse;
import com.shopwise.Dto.salary.SalaryPaymentRequest;
import com.shopwise.Dto.salary.SalaryPaymentResponse;
import com.shopwise.Services.salary.SalaryPaymentService;
import com.shopwise.models.SalaryPayment;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for salary payment management
 */
@RestController
@RequestMapping("/api/salary-payments")
@RequiredArgsConstructor
@Slf4j
public class SalaryPaymentController {

    private final SalaryPaymentService salaryPaymentService;

    /**
     * Create a new salary payment
     *
     * @param request The salary payment request
     * @param authentication The authenticated user
     * @return The created salary payment
     */
    @PostMapping
    public ResponseEntity<?> createSalaryPayment(@Valid @RequestBody SalaryPaymentRequest request, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            SalaryPayment salaryPayment = salaryPaymentService.createSalaryPayment(
                request.getEmployeeId(),
                request.getAmount(),
                request.getPaymentDate(),
                currentUser
            );
            
            SalaryPaymentResponse response = mapToResponse(salaryPayment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating salary payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while creating the salary payment"));
        }
    }

    /**
     * Get a salary payment by ID
     *
     * @param paymentId The payment ID
     * @param authentication The authenticated user
     * @return The salary payment
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getSalaryPayment(@PathVariable UUID paymentId, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            SalaryPayment salaryPayment = salaryPaymentService.getSalaryPayment(paymentId, currentUser);
            
            SalaryPaymentResponse response = mapToResponse(salaryPayment);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting salary payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving the salary payment"));
        }
    }

    /**
     * Get all salary payments for an employee
     *
     * @param employeeId The employee ID
     * @param authentication The authenticated user
     * @return List of salary payments
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeSalaryPayments(@PathVariable UUID employeeId, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            List<SalaryPayment> salaryPayments = salaryPaymentService.getEmployeeSalaryPayments(employeeId, currentUser);
            
            List<SalaryPaymentResponse> response = salaryPayments.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting employee salary payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving the employee's salary payments"));
        }
    }

    /**
     * Get all salary payments for a business
     *
     * @param request The HTTP request containing cookies
     * @param authentication The authenticated user
     * @return List of salary payments
     */
    @GetMapping("/business")
    public ResponseEntity<?> getBusinessSalaryPayments(HttpServletRequest request, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract business ID from cookies
            UUID businessId = getSelectedBusinessId(request);
            if (businessId == null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "No business selected"));
            }
            
            List<SalaryPayment> salaryPayments = salaryPaymentService.getBusinessSalaryPayments(businessId, currentUser);
            
            List<SalaryPaymentResponse> response = salaryPayments.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting business salary payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving the business's salary payments"));
        }
    }

    /**
     * Delete a salary payment
     *
     * @param paymentId The payment ID
     * @param authentication The authenticated user
     * @return Success/failure response
     */
    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deleteSalaryPayment(@PathVariable UUID paymentId, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            boolean deleted = salaryPaymentService.deleteSalaryPayment(paymentId, currentUser);
            
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Salary payment deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Failed to delete salary payment"));
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting salary payment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while deleting the salary payment"));
        }
    }

    /**
     * Extract the selected business ID from cookies
     *
     * @param request The HTTP request
     * @return The selected business ID, or null if not found
     */
    private UUID getSelectedBusinessId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("selectedBusiness".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                    try {
                        return UUID.fromString(cookie.getValue());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid business ID in cookie: {}", cookie.getValue());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Map a SalaryPayment entity to a SalaryPaymentResponse DTO
     *
     * @param salaryPayment The salary payment entity
     * @return The salary payment response DTO
     */
    private SalaryPaymentResponse mapToResponse(SalaryPayment salaryPayment) {
        SalaryPaymentResponse response = new SalaryPaymentResponse();
        response.setId(salaryPayment.getId());
        response.setEmployeeId(salaryPayment.getEmployee().getId());
        response.setEmployeeName(salaryPayment.getEmployee().getName());
        response.setEmployeeRole(salaryPayment.getEmployee().getRole().toString());
        response.setAmount(salaryPayment.getAmount());
        response.setPaymentDate(salaryPayment.getPaymentDate());
        response.setBusinessId(salaryPayment.getEmployee().getBusiness().getId());
        response.setBusinessName(salaryPayment.getEmployee().getBusiness().getName());
        return response;
    }
}
