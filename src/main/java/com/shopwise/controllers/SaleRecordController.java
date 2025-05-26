package com.shopwise.controllers;

import com.shopwise.Dto.salerecord.SaleRecordRequest;
import com.shopwise.Dto.salerecord.SaleRecordResponse;
import com.shopwise.Dto.salerecord.SaleRecordUpdateRequest;
import com.shopwise.Dto.salerecord.SalesWithTotalResponse;
import com.shopwise.Services.salerecord.SaleRecordException;
import com.shopwise.Services.salerecord.SaleRecordService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SaleRecordController {

    /**
     * Helper method to extract business ID from cookies
     * 
     * @param request The HTTP request containing cookies
     * @return ResponseEntity with error if business not selected, or null if business ID was successfully extracted
     */
    private ResponseEntity<Map<String, String>> validateAndExtractBusinessId(HttpServletRequest request, UUID[] businessIdHolder) {
        // Extract business ID from cookie
        String businessIdStr = null;
        Cookie[] cookies = request.getCookies();
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
        try {
            businessIdHolder[0] = UUID.fromString(businessIdStr);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid business ID format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        return null; // No error, business ID successfully extracted
    }

    private final SaleRecordService saleRecordService;

    /**
     * Logs a new sale for a business
     * 
     * @param request Sale record details containing:
     *               - productId (required): ID of the product being sold
     *               - packetsSold: Number of packets sold (must be >= 0)
     *               - piecesSold: Number of individual pieces sold (must be >= 0)
     *               - saleTime: Time of the sale (defaults to current time if not provided)
     *               - manuallyAdjusted: Whether the sale was manually adjusted
     *               - loggedLater: Whether the sale is being logged after it occurred
     *               - notes: Additional notes about the sale
     *               - actualSaleTime: The actual time of sale if logged later
     * @param httpRequest HTTP request containing cookies with the selected business
     * @param authentication Authentication object with user details
     * @return Created sale record with details and total value
     */
    @PostMapping("/log")
    public ResponseEntity<?> logSale(
            @Valid @RequestBody SaleRecordRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        log.info("Received request to log sale for product ID: {}", request.getProductId());
        
        try {
            // Extract business ID from cookies
            UUID[] businessIdHolder = new UUID[1];
            ResponseEntity<Map<String, String>> errorResponse = validateAndExtractBusinessId(httpRequest, businessIdHolder);
            if (errorResponse != null) {
                log.warn("Business validation failed when logging sale");
                return errorResponse;
            }
            UUID businessId = businessIdHolder[0];
            
            // Validate product ID
            if (request.getProductId() == null) {
                Map<String, String> validationError = new HashMap<>();
                validationError.put("error", "Product ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }
            
            // Validate quantities
            if (!request.isValidSale()) {
                Map<String, String> validationError = new HashMap<>();
                validationError.put("error", "At least one of packetsSold or piecesSold must be provided and greater than zero");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }
            
            // Validate actual sale time if logged later
            if (request.isLoggedLater() && request.getActualSaleTime() == null) {
                Map<String, String> validationError = new HashMap<>();
                validationError.put("error", "Actual sale time must be provided when logged later is true");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
            }
            
            // Log the sale
            log.info("Logging sale for business ID: {} and product ID: {}", businessId, request.getProductId());
            SaleRecordResponse createdSale = saleRecordService.logSale(businessId, request);
            
            // Return success response
            log.info("Successfully logged sale with ID: {}", createdSale.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSale);
        } catch (SaleRecordException e) {
            log.warn("Sale record exception when logging sale: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid argument when logging sale: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error when logging sale", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while processing your request. Please try again later.");
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
     * Gets all sales for the current business
     * 
     * @param httpRequest HTTP request containing cookies
     * @param authentication Authentication object with user details
     * @return List of sale records for the business
     */
    @GetMapping("/business")
    public ResponseEntity<?> getSalesByBusiness(
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Extract business ID from cookies
            UUID[] businessIdHolder = new UUID[1];
            ResponseEntity<Map<String, String>> errorResponse = validateAndExtractBusinessId(httpRequest, businessIdHolder);
            if (errorResponse != null) {
                return errorResponse;
            }
            
            List<SaleRecordResponse> sales = saleRecordService.getSalesByBusiness(businessIdHolder[0]);
            return ResponseEntity.ok(sales);
        } catch (SaleRecordException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting sales by business", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Gets sales for the current business on a specific date
     * 
     * @param date Date to filter sales
     * @param httpRequest HTTP request containing cookies
     * @param authentication Authentication object with user details
     * @return Sales records and total amount for the business on the specified date
     */
    @GetMapping("/date")
    public ResponseEntity<?> getSalesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Extract business ID from cookies
            UUID[] businessIdHolder = new UUID[1];
            ResponseEntity<Map<String, String>> errorResponse = validateAndExtractBusinessId(httpRequest, businessIdHolder);
            if (errorResponse != null) {
                return errorResponse;
            }
            
            List<SaleRecordResponse> sales = saleRecordService.getSalesByDate(businessIdHolder[0], date);
            
            // Calculate the total amount of money made that day
            double totalAmount = sales.stream()
                    .mapToDouble(SaleRecordResponse::getTotalSaleValue)
                    .sum();
            
            // Create response with sales list and total amount
            SalesWithTotalResponse response = SalesWithTotalResponse.builder()
                    .sales(sales)
                    .totalAmount(totalAmount)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (SaleRecordException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting sales by date", e);
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
