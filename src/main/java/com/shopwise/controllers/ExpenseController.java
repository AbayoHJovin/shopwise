package com.shopwise.controllers;

import com.shopwise.Dto.expense.ExpenseRequest;
import com.shopwise.Dto.expense.ExpenseResponse;
import com.shopwise.Services.expense.ExpenseException;
import com.shopwise.Services.expense.ExpenseService;
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

    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> createExpense(@PathVariable UUID businessId,
                                         @Valid @RequestBody ExpenseRequest request,
                                         Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
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

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<?> deleteExpense(@PathVariable UUID expenseId,
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
