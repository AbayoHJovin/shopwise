package com.shopwise.controllers;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.discovery.BusinessDiscoveryDto;
import com.shopwise.Dto.discovery.LocationRequestDto;
import com.shopwise.Dto.discovery.LocationSearchRequestDto;
import com.shopwise.Dto.discovery.PaginatedResponseDto;
import com.shopwise.Dto.discovery.ProductDiscoveryDto;
import com.shopwise.Dto.discovery.ProductPageRequestDto;
import com.shopwise.Dto.discovery.ProductPageResponseDto;
import com.shopwise.Services.business.BusinessException;
import com.shopwise.Services.business.BusinessService;
import com.shopwise.Services.discovery.BusinessDiscoveryException;
import com.shopwise.Services.discovery.BusinessDiscoveryService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for location-based business discovery
 */
@RestController
@RequestMapping("/api/businesses/discovery")
@RequiredArgsConstructor
@Slf4j
public class BusinessDiscoveryController {

    private final BusinessDiscoveryService businessDiscoveryService;
    private final BusinessService businessService;

    /**
     * Get businesses nearest to the user's location
     */
    @PostMapping("/nearest")
    public ResponseEntity<?> getNearestBusinesses(@RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = businessDiscoveryService.getNearestBusinesses(request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting nearest businesses", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get products for a specific business
     */
    @PostMapping("/{businessId}/products")
    public ResponseEntity<?> getProductsForBusiness(
            @PathVariable UUID businessId,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<ProductDiscoveryDto> response = 
                    businessDiscoveryService.getProductsForBusiness(businessId, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting products for business", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Filter businesses by province
     */
    @PostMapping("/filter/province/{province}")
    public ResponseEntity<?> getBusinessesByProvince(
            @PathVariable String province,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesByProvince(province, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error filtering businesses by province", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Filter businesses by district
     */
    @PostMapping("/filter/district/{district}")
    public ResponseEntity<?> getBusinessesByDistrict(
            @PathVariable String district,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesByDistrict(district, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error filtering businesses by district", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Filter businesses by sector
     */
    @PostMapping("/filter/sector/{sector}")
    public ResponseEntity<?> getBusinessesBySector(
            @PathVariable String sector,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesBySector(sector, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error filtering businesses by sector", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Filter businesses by cell
     */
    @PostMapping("/filter/cell/{cell}")
    public ResponseEntity<?> getBusinessesByCell(
            @PathVariable String cell,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesByCell(cell, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error filtering businesses by cell", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get business details by ID without requiring authentication
     * This endpoint is public and can be accessed by anyone
     */
    @GetMapping("/get-by-id/{businessId}")
    public ResponseEntity<?> getBusinessById(@PathVariable UUID businessId) {
        try {
            // Get the business from the repository directly instead of using the service
            // that requires authentication
            BusinessDto business = businessDiscoveryService.getPublicBusinessDetails(businessId);
            return ResponseEntity.ok(business);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting business details", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get products for a business with pagination and sorting
     * This endpoint is public and can be accessed by anyone
     */
    @PostMapping("/products/{businessId}")
    public ResponseEntity<?> getProductsForBusinessPaginated(
            @PathVariable UUID businessId,
            @RequestBody @Valid ProductPageRequestDto request) {
        try {
            ProductPageResponseDto response = businessDiscoveryService.getProductsForBusinessPaginated(businessId, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting products for business", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Filter businesses by village
     */
    @PostMapping("/filter/village/{village}")
    public ResponseEntity<?> getBusinessesByVillage(
            @PathVariable String village,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesByVillage(village, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error filtering businesses by village", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Search for businesses that have products with names containing the given string
     */
    @PostMapping("/search/product/{productName}")
    public ResponseEntity<?> searchBusinessesByProductName(
            @PathVariable String productName,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.searchBusinessesByProductName(productName, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error searching businesses by product name", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Search for businesses by name
     */
    @PostMapping("/search/name/{businessName}")
    public ResponseEntity<?> searchBusinessesByName(
            @PathVariable String businessName,
            @RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.searchBusinessesByName(businessName, request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error searching businesses by name", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Search for businesses by name and location
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<?> searchBusinessesByNameAndLocation(
            @RequestBody @Valid LocationSearchRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.searchBusinessesByNameAndLocation(request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error in advanced business search", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get businesses within a specified radius from the user's location
     */
    @PostMapping("/within-radius")
    public ResponseEntity<?> getBusinessesWithinRadius(@RequestBody @Valid LocationRequestDto request) {
        try {
            PaginatedResponseDto<BusinessDiscoveryDto> response = 
                    businessDiscoveryService.getBusinessesWithinRadius(request);
            return ResponseEntity.ok(response);
        } catch (BusinessDiscoveryException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error getting businesses within radius", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

}
