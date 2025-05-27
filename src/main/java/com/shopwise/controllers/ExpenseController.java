package com.shopwise.controllers;

import com.shopwise.Dto.expense.ExpenseRequest;
import com.shopwise.Dto.expense.ExpenseResponse;
import com.shopwise.Services.expense.ExpenseException;
import com.shopwise.Services.expense.ExpenseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/create")
    public ResponseEntity<?> createExpense(@Valid @RequestBody ExpenseRequest request,
                                         HttpServletRequest httpRequest) {
        try {
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
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No business selected. Please select a business first.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            UUID businessId = UUID.fromString(businessIdStr);
            ExpenseResponse createdExpense = expenseService.createExpense(businessId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
        } catch (ExpenseException e) {
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
    public ResponseEntity<?> getExpensesByBusiness(@PathVariable UUID businessId,
                                                 Authentication authentication) {
        try {
            List<ExpenseResponse> expenses = expenseService.getExpensesByBusiness(businessId);
            return ResponseEntity.ok(expenses);
        } catch (ExpenseException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/business/{businessId}/date-range")
    public ResponseEntity<?> getExpensesByDateRange(
            @PathVariable UUID businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        try {
            List<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(businessId, startDate, endDate);
            return ResponseEntity.ok(expenses);
        } catch (ExpenseException e) {
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
     * Get expenses for a specific date
     * 
     * @param date The specific date to filter expenses (format: yyyy-MM-dd)
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return List of expenses for the specified date
     */
    @GetMapping
    public ResponseEntity<?> getExpensesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
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
            
            // Check if business ID is available
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No business selected. Please select a business first.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Parse business ID
            UUID businessId;
            try {
                businessId = UUID.fromString(businessIdStr);
            } catch (IllegalArgumentException e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid business ID format.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Use the existing date range service method with the same date for start and end
            List<ExpenseResponse> expenses = expenseService.getExpensesByDateRange(businessId, date, date);
            
            // Calculate total amount
            double totalAmount = expenses.stream()
                    .mapToDouble(ExpenseResponse::getAmount)
                    .sum();
            
            // Create a response with just the expenses and total amount
            Map<String, Object> response = new HashMap<>();
            response.put("expenses", expenses);
            response.put("totalAmount", totalAmount);
            
            return ResponseEntity.ok(response);
        } catch (ExpenseException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long expenseId,
                                         Authentication authentication) {
        try {
            expenseService.deleteExpense(expenseId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Expense deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ExpenseException e) {
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
