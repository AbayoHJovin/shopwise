package com.shopwise.controllers;

import com.shopwise.Dto.dailysummary.DailySummaryResponse;
import com.shopwise.Services.dailysummary.DailySummaryException;
import com.shopwise.Services.dailysummary.DailySummaryService;
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
@RequestMapping("/api/daily-summaries")
@RequiredArgsConstructor
public class DailySummaryController {

    private final DailySummaryService dailySummaryService;

    /**
     * Gets all daily summaries for a business
     * 
     * @param businessId Business ID
     * @param authentication Authentication object with user details
     * @return List of daily summaries for the business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getDailySummaries(
            @PathVariable UUID businessId,
            Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
            List<DailySummaryResponse> summaries = dailySummaryService.getDailySummaries(businessId);
            return ResponseEntity.ok(summaries);
        } catch (DailySummaryException e) {
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
     * Gets a daily summary for a business on a specific date
     * 
     * @param businessId Business ID
     * @param date Date to filter summary
     * @param authentication Authentication object with user details
     * @return Daily summary for the business on the specified date
     */
    @GetMapping("/business/{businessId}/date")
    public ResponseEntity<?> getSummaryByDate(
            @PathVariable UUID businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        try {
            DailySummaryResponse summary = dailySummaryService.getSummaryByDate(businessId, date);
            return ResponseEntity.ok(summary);
        } catch (DailySummaryException e) {
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
     * Logs a daily action for a business
     * 
     * @param businessId Business ID
     * @param request Map containing the description
     * @param authentication Authentication object with user details
     * @return Success message
     */
    @PostMapping("/business/{businessId}/log")
    public ResponseEntity<?> logDailyAction(
            @PathVariable UUID businessId,
            @Valid @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            String description = request.get("description");
            if (description == null || description.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Description is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            dailySummaryService.logDailyAction(businessId, description);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Daily action logged successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (DailySummaryException e) {
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
