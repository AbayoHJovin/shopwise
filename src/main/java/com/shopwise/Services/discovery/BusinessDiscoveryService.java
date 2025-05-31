package com.shopwise.Services.discovery;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.discovery.*;
import jakarta.validation.Valid;

import java.util.UUID;

/**
 * Service interface for location-based business discovery
 */
public interface BusinessDiscoveryService {
    
    /**
     * Get businesses nearest to the user's location
     * 
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses sorted by distance
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getNearestBusinesses(LocationRequestDto request);
    
    /**
     * Get products for a specific business with pagination
     * 
     * @param businessId The business ID
     * @param request The location request with pagination info
     * @return Paginated list of products
     */
    PaginatedResponseDto<ProductDiscoveryDto> getProductsForBusiness(UUID businessId, LocationRequestDto request);
    
    /**
     * Filter businesses by province
     * 
     * @param province The province name
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses in the specified province
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByProvince(String province, LocationRequestDto request);
    
    /**
     * Filter businesses by district
     * 
     * @param district The district name
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses in the specified district
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByDistrict(String district, LocationRequestDto request);
    
    /**
     * Filter businesses by sector
     * 
     * @param sector The sector name
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses in the specified sector
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesBySector(String sector, LocationRequestDto request);
    
    /**
     * Filter businesses by cell
     * 
     * @param cell The cell name
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses in the specified cell
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByCell(String cell, LocationRequestDto request);
    
    /**
     * Filter businesses by village
     * 
     * @param village The village name
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses in the specified village
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByVillage(String village, LocationRequestDto request);
    
    /**
     * Search for businesses that have products with names containing the given string
     * 
     * @param productName The product name to search for
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses that have matching products, sorted by distance
     */
    PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByProductName(String productName, LocationRequestDto request);
    
    /**
     * Search for businesses by name
     * 
     * @param businessName The business name to search for
     * @param request The location request with user coordinates and pagination info
     * @return Paginated list of businesses with names containing the given string
     */
    PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByName(String businessName, LocationRequestDto request);
    
    /**
     * Search for businesses by name and location
     * 
     * @param searchRequest The search request with business name, location filters, and pagination info
     * @return Paginated list of businesses matching the search criteria
     */
    PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByNameAndLocation(LocationSearchRequestDto searchRequest);
    
    /**
     * Get businesses within a specified radius from the user's location
     * 
     * @param request The location request with user coordinates, radius, and pagination info
     * @return Paginated list of businesses within the specified radius, sorted by distance
     */
    PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesWithinRadius(LocationRequestDto request);
    
    /**
     * Get public business details by ID without requiring authentication
     * 
     * @param businessId The ID of the business to retrieve
     * @return The business details as a BusinessDto
     * @throws BusinessDiscoveryException if the business is not found
     */
    BusinessDto getPublicBusinessDetails(UUID businessId);
    
    /**
     * Get products for a business with pagination and sorting
     * 
     * @param businessId The ID of the business to retrieve products for
     * @param request The pagination and sorting parameters
     * @return A paginated list of products
     * @throws BusinessDiscoveryException if the business is not found
     */
    ProductPageResponseDto getProductsForBusinessPaginated(UUID businessId, @Valid ProductPageRequestDto request);
}
