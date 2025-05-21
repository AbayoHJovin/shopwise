package com.shopwise.controllers;

import com.shopwise.Dto.salerecord.SaleRecordRequest;
import com.shopwise.Dto.salerecord.SaleRecordResponse;
import com.shopwise.Dto.salerecord.SaleRecordUpdateRequest;
import com.shopwise.Services.salerecord.SaleRecordException;
import com.shopwise.Services.salerecord.SaleRecordService;
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
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleRecordController {

    private final SaleRecordService saleRecordService;

    /**
     * Logs a new sale for a business
     * 
     * @param businessId Business ID
     * @param request Sale record details
     * @param authentication Authentication object with user details
     * @return Created sale record
     */
    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> logSale(
            @PathVariable UUID businessId,
            @Valid @RequestBody SaleRecordRequest request,
            Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
            SaleRecordResponse createdSale = saleRecordService.logSale(businessId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSale);
        } catch (SaleRecordException e) {
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
     * Gets all sales for a specific product
     * 
     * @param productId Product ID
     * @param authentication Authentication object with user details
     * @return List of sale records for the product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getSalesByProduct(
            @PathVariable UUID productId,
            Authentication authentication) {
        try {
            List<SaleRecordResponse> sales = saleRecordService.getSalesByProduct(productId);
            return ResponseEntity.ok(sales);
        } catch (SaleRecordException e) {
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
     * Gets all sales for a business
     * 
     * @param businessId Business ID
     * @param authentication Authentication object with user details
     * @return List of sale records for the business
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getSalesByBusiness(
            @PathVariable UUID businessId,
            Authentication authentication) {
        try {
            List<SaleRecordResponse> sales = saleRecordService.getSalesByBusiness(businessId);
            return ResponseEntity.ok(sales);
        } catch (SaleRecordException e) {
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
     * Gets sales for a business on a specific date
     * 
     * @param businessId Business ID
     * @param date Date to filter sales
     * @param authentication Authentication object with user details
     * @return List of sale records for the business on the specified date
     */
    @GetMapping("/business/{businessId}/date")
    public ResponseEntity<?> getSalesByDate(
            @PathVariable UUID businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        try {
            List<SaleRecordResponse> sales = saleRecordService.getSalesByDate(businessId, date);
            return ResponseEntity.ok(sales);
        } catch (SaleRecordException e) {
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
     * Updates an existing sale record
     * 
     * @param saleId Sale record ID
     * @param request Updated sale record details
     * @param authentication Authentication object with user details
     * @return Updated sale record
     */
    @PutMapping("/{saleId}")
    public ResponseEntity<?> updateSale(
            @PathVariable UUID saleId,
            @Valid @RequestBody SaleRecordUpdateRequest request,
            Authentication authentication) {
        try {
            SaleRecordResponse updatedSale = saleRecordService.updateSale(saleId, request);
            return ResponseEntity.ok(updatedSale);
        } catch (SaleRecordException e) {
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
