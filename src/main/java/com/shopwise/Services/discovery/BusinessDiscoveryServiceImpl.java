package com.shopwise.Services.discovery;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.LocationDto;
import com.shopwise.Dto.discovery.BusinessDiscoveryDto;
import com.shopwise.Dto.discovery.LocationRequestDto;
import com.shopwise.Dto.discovery.LocationSearchRequestDto;
import com.shopwise.Dto.discovery.PaginatedResponseDto;
import com.shopwise.Dto.discovery.ProductDiscoveryDto;
import com.shopwise.Dto.discovery.ProductPageRequestDto;
import com.shopwise.Dto.discovery.ProductPageResponseDto;
import com.shopwise.Dto.discovery.PublicProductDto;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ProductImageRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.models.Business;
import com.shopwise.models.Location;
import com.shopwise.models.Product;
import com.shopwise.models.ProductImage;
import com.shopwise.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the BusinessDiscoveryService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessDiscoveryServiceImpl implements BusinessDiscoveryService {

    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    
    private static final double DEFAULT_RADIUS_KM = 10.0; // Default radius of 10 kilometers

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getNearestBusinesses(LocationRequestDto request) {
        validateLocationRequest(request);
        
        // Get all businesses with coordinates
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findAllWithCoordinates(pageable);
        
        // Calculate distances and sort by proximity
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .filter(business -> business.getLocation() != null 
                        && business.getLocation().getLatitude() != null 
                        && business.getLocation().getLongitude() != null)
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .sorted(Comparator.comparing(BusinessDiscoveryDto::getDistanceKm))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<ProductDiscoveryDto> getProductsForBusiness(UUID businessId, LocationRequestDto request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessDiscoveryException("Business not found with ID: " + businessId));
        
        // Get products with pagination
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Product> productPage = productRepository.findByBusinessId(businessId, pageable);
        
        // Map to DTOs
        List<ProductDiscoveryDto> productDtos = productPage.getContent().stream()
                .map(product -> mapToProductDiscoveryDto(product, business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(productDtos, productPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByProvince(String province, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByLocationProvinceIgnoreCase(province, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByDistrict(String district, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByLocationDistrictIgnoreCase(district, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesBySector(String sector, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByLocationSectorIgnoreCase(sector, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByCell(String cell, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByLocationCellIgnoreCase(cell, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesByVillage(String village, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByLocationVillageIgnoreCase(village, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByProductName(String productName, LocationRequestDto request) {
        validateLocationRequest(request);
        
        // Find businesses that have products with the given name
        List<UUID> businessIds = productRepository.findBusinessIdsWithProductNameContaining(productName);
        
        if (businessIds.isEmpty()) {
            return createEmptyPaginatedResponse(request.getSkip(), request.getLimit());
        }
        
        // Get businesses by IDs
        List<Business> businesses = businessRepository.findAllById(businessIds);
        
        // Calculate distances and sort by proximity
        List<BusinessDiscoveryDto> businessDtos = businesses.stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .sorted(Comparator.comparing(BusinessDiscoveryDto::getDistanceKm))
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = Math.min(request.getSkip(), businessDtos.size());
        int end = Math.min(start + request.getLimit(), businessDtos.size());
        List<BusinessDiscoveryDto> paginatedDtos = businessDtos.subList(start, end);
        
        return createPaginatedResponse(paginatedDtos, businessDtos.size(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByName(String businessName, LocationRequestDto request) {
        validateLocationRequest(request);
        
        Pageable pageable = PageRequest.of(
                request.getSkip() / request.getLimit(),
                request.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage = businessRepository.findByNameContainingIgnoreCase(businessName, pageable);
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), request.getSkip(), request.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> searchBusinessesByNameAndLocation(LocationSearchRequestDto searchRequest) {
        validateLocationRequest(searchRequest);
        
        Pageable pageable = PageRequest.of(
                searchRequest.getSkip() / searchRequest.getLimit(),
                searchRequest.getLimit(),
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Business> businessPage;
        
        // Determine which location filter to apply
        if (searchRequest.getVillage() != null && !searchRequest.getVillage().isEmpty()) {
            businessPage = businessRepository.findByNameContainingIgnoreCaseAndLocationVillageIgnoreCase(
                    searchRequest.getBusinessName(), searchRequest.getVillage(), pageable);
        } else if (searchRequest.getCell() != null && !searchRequest.getCell().isEmpty()) {
            businessPage = businessRepository.findByNameContainingIgnoreCaseAndLocationCellIgnoreCase(
                    searchRequest.getBusinessName(), searchRequest.getCell(), pageable);
        } else if (searchRequest.getSector() != null && !searchRequest.getSector().isEmpty()) {
            businessPage = businessRepository.findByNameContainingIgnoreCaseAndLocationSectorIgnoreCase(
                    searchRequest.getBusinessName(), searchRequest.getSector(), pageable);
        } else if (searchRequest.getDistrict() != null && !searchRequest.getDistrict().isEmpty()) {
            businessPage = businessRepository.findByNameContainingIgnoreCaseAndLocationDistrictIgnoreCase(
                    searchRequest.getBusinessName(), searchRequest.getDistrict(), pageable);
        } else if (searchRequest.getProvince() != null && !searchRequest.getProvince().isEmpty()) {
            businessPage = businessRepository.findByNameContainingIgnoreCaseAndLocationProvinceIgnoreCase(
                    searchRequest.getBusinessName(), searchRequest.getProvince(), pageable);
        } else {
            // If no location filter, just search by name
            businessPage = businessRepository.findByNameContainingIgnoreCase(searchRequest.getBusinessName(), pageable);
        }
        
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .map(business -> mapToBusinessDiscoveryDto(business, searchRequest.getLatitude(), searchRequest.getLongitude()))
                .collect(Collectors.toList());
        
        return createPaginatedResponse(businessDtos, businessPage.getTotalElements(), searchRequest.getSkip(), searchRequest.getLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<BusinessDiscoveryDto> getBusinessesWithinRadius(LocationRequestDto request) {
        validateLocationRequest(request);
        
        double radius = request.getRadius() != null ? request.getRadius() : DEFAULT_RADIUS_KM;
        
        // Get all businesses with coordinates
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Get all businesses, we'll filter by distance
        Page<Business> businessPage = businessRepository.findAllWithCoordinates(pageable);
        
        // Filter businesses within radius and sort by distance
        List<BusinessDiscoveryDto> businessDtos = businessPage.getContent().stream()
                .filter(business -> business.getLocation() != null 
                        && business.getLocation().getLatitude() != null 
                        && business.getLocation().getLongitude() != null)
                .map(business -> mapToBusinessDiscoveryDto(business, request.getLatitude(), request.getLongitude()))
                .filter(dto -> dto.getDistanceKm() <= radius)
                .sorted(Comparator.comparing(BusinessDiscoveryDto::getDistanceKm))
                .collect(Collectors.toList());
        
        // Apply pagination manually
        int start = Math.min(request.getSkip(), businessDtos.size());
        int end = Math.min(start + request.getLimit(), businessDtos.size());
        List<BusinessDiscoveryDto> paginatedDtos = businessDtos.subList(start, end);
        
        return createPaginatedResponse(paginatedDtos, businessDtos.size(), request.getSkip(), request.getLimit());
    }
    
    /**
     * Map a Business entity to a BusinessDiscoveryDto with distance information
     */
    private BusinessDiscoveryDto mapToBusinessDiscoveryDto(Business business, Double userLat, Double userLon) {
        // Calculate distance if coordinates are available
        Double distanceKm = null;
        String formattedDistance = null;
        
        if (business.getLocation() != null 
                && business.getLocation().getLatitude() != null 
                && business.getLocation().getLongitude() != null) {
            distanceKm = GeoUtils.calculateDistance(
                    userLat, userLon,
                    business.getLocation().getLatitude(),
                    business.getLocation().getLongitude()
            );
            formattedDistance = GeoUtils.formatDistance(distanceKm);
        }
        
        // Count products
        int productCount = (int) productRepository.countByBusinessId(business.getId());
        
        // Map location
        LocationDto locationDto = null;
        if (business.getLocation() != null) {
            locationDto = LocationDto.builder()
                    .province(business.getLocation().getProvince())
                    .district(business.getLocation().getDistrict())
                    .sector(business.getLocation().getSector())
                    .cell(business.getLocation().getCell())
                    .village(business.getLocation().getVillage())
                    .latitude(business.getLocation().getLatitude())
                    .longitude(business.getLocation().getLongitude())
                    .build();
        }
        
        return BusinessDiscoveryDto.builder()
                .id(business.getId())
                .name(business.getName())
                .location(locationDto)
                .about(business.getAbout())
                .websiteLink(business.getWebsiteLink())
                .productCount(productCount)
                .distanceKm(distanceKm)
                .formattedDistance(formattedDistance)
                .build();
    }
    
    /**
     * Map a Product entity to a ProductDiscoveryDto with distance information
     */
    private ProductDiscoveryDto mapToProductDiscoveryDto(Product product, Business business, Double userLat, Double userLon) {
        // Calculate distance if coordinates are available
        Double distanceKm = null;
        String formattedDistance = null;
        
        if (business.getLocation() != null 
                && business.getLocation().getLatitude() != null 
                && business.getLocation().getLongitude() != null) {
            distanceKm = GeoUtils.calculateDistance(
                    userLat, userLon,
                    business.getLocation().getLatitude(),
                    business.getLocation().getLongitude()
            );
            formattedDistance = GeoUtils.formatDistance(distanceKm);
        }
        
        // Get product images
        List<ProductImage> productImages = productImageRepository.findByProductId(product.getId());
        List<String> imageUrls = productImages.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        
        // Calculate price based on pricePerItem and packets * itemsPerPacket
        double totalPrice = product.getPricePerItem() * product.getPackets() * product.getItemsPerPacket();
        
        // Calculate packets available and additional units
        int fullPacketsAvailable = product.getPackets();
        int additionalUnits = 0;
        int itemsPerPacket = product.getItemsPerPacket();
        
        // Calculate pricing details
        BigDecimal unitPrice = new BigDecimal(product.getPricePerItem());
        BigDecimal packetPrice = unitPrice.multiply(new BigDecimal(itemsPerPacket));
        BigDecimal fulfillmentCost = new BigDecimal(product.getFulfillmentCost());
        
        return ProductDiscoveryDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(new java.math.BigDecimal(totalPrice))
                .quantity(product.getPackets() * product.getItemsPerPacket())
                .businessId(business.getId())
                .businessName(business.getName())
                .imageUrls(imageUrls)
                .distanceKm(distanceKm)
                .formattedDistance(formattedDistance)
                .fullPacketsAvailable(fullPacketsAvailable)
                .additionalUnits(additionalUnits)
                .itemsPerPacket(itemsPerPacket)
                .unitPrice(unitPrice)
                .fulfillmentCost(fulfillmentCost)
                .packetPrice(packetPrice)
                .build();
    }
    
    /**
     * Create a paginated response
     */
    private <T> PaginatedResponseDto<T> createPaginatedResponse(List<T> data, long totalCount, int skip, int limit) {
        return PaginatedResponseDto.<T>builder()
                .data(data)
                .totalCount((int) totalCount)
                .skip(skip)
                .limit(limit)
                .hasMore(skip + limit < totalCount)
                .build();
    }
    
    /**
     * Create an empty paginated response
     */
    private <T> PaginatedResponseDto<T> createEmptyPaginatedResponse(int skip, int limit) {
        return PaginatedResponseDto.<T>builder()
                .data(Collections.emptyList())
                .totalCount(0)
                .skip(skip)
                .limit(limit)
                .hasMore(false)
                .build();
    }
    
    /**
     * Validate location request
     */
    private void validateLocationRequest(LocationRequestDto request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessDiscoveryException("Location access is required. Please allow access to your location to continue.");
        }
    }
    
    /**
     * Validate location search request
     */
    private void validateLocationRequest(LocationSearchRequestDto request) {
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new BusinessDiscoveryException("Location access is required. Please allow access to your location to continue.");
        }
    }
    
    /**
     * Get public business details by ID without requiring authentication
     * 
     * @param businessId The ID of the business to retrieve
     * @return The business details as a BusinessDto
     * @throws BusinessDiscoveryException if the business is not found
     */
    @Override
    @Transactional
    public BusinessDto getPublicBusinessDetails(UUID businessId) {
        // Find the business by ID
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessDiscoveryException("Business not found with ID: " + businessId));
        
        // Map the business to a BusinessDto

        return BusinessDto.builder()
                .id(business.getId())
                .name(business.getName())
                .about(business.getAbout())
                .websiteLink(business.getWebsiteLink())
                .location(mapLocationToDto(business.getLocation()))
                .productCount(business.getProducts() != null ? business.getProducts().size() : 0)
                .employeeCount(business.getEmployees() != null ? business.getEmployees().size() : 0)
                .open(business.isOpen())
                .build();
    }
    
    /**
     * Map a Location entity to a LocationDto
     */
    private LocationDto mapLocationToDto(Location location) {
        if (location == null) {
            return null;
        }

        
        return LocationDto.builder()
                .province(location.getProvince())
                .district(location.getDistrict())
                .sector(location.getSector())
                .cell(location.getCell())
                .village(location.getVillage())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
    }
    
    /**
     * Get products for a business with pagination and sorting
     * 
     * @param businessId The ID of the business to retrieve products for
     * @param request The pagination and sorting parameters
     * @return A paginated list of products
     * @throws BusinessDiscoveryException if the business is not found
     */
    @Override
    @Transactional
    public ProductPageResponseDto getProductsForBusinessPaginated(UUID businessId, ProductPageRequestDto request) {
        // Find the business by ID
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new BusinessDiscoveryException("Business not found with ID: " + businessId));
        
        // Create a Pageable object for pagination and sorting
        Pageable pageable = createPageable(request);
        
        // Get products with pagination and sorting
        Page<Product> productPage;
        if (request.getSearchTerm() != null && !request.getSearchTerm().trim().isEmpty()) {
            // Search products by name if search term is provided
            productPage = productRepository.findByBusinessIdAndNameContainingIgnoreCase(
                    businessId, request.getSearchTerm().trim(), pageable);
        } else {
            // Get all products for the business
            productPage = productRepository.findByBusinessId(businessId, pageable);
        }
        
        // Map products to DTOs
        List<PublicProductDto> productDtos = productPage.getContent().stream()
                .map(this::mapToPublicProductDto)
                .collect(Collectors.toList());
        
        // Create response
        return ProductPageResponseDto.builder()
                .products(productDtos)
                .totalCount(productPage.getTotalElements())
                .skip(request.getSkip())
                .limit(request.getLimit())
                .hasMore((request.getSkip() + productDtos.size()) < productPage.getTotalElements())
                .sortBy(request.getSortBy())
                .sortDirection(request.getSortDirection())
                .businessName(business.getName())
                .build();
    }
    
    /**
     * Create a Pageable object for pagination and sorting
     */
    private Pageable createPageable(ProductPageRequestDto request) {
        // Validate and sanitize sort field
        String sortBy = validateSortField(request.getSortBy());
        
        // Create sort direction
        Sort.Direction direction = "desc".equalsIgnoreCase(request.getSortDirection()) 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        // Create sort object
        Sort sort = Sort.by(direction, sortBy);
        
        // Create pageable with skip/limit
        return PageRequest.of(request.getSkip() / request.getLimit(), request.getLimit(), sort);
    }
    
    /**
     * Validate and sanitize sort field
     */
    private String validateSortField(String sortField) {
        // List of allowed sort fields
        List<String> allowedFields = Arrays.asList("name", "pricePerItem", "packets", "itemsPerPacket");
        
        // Default to "name" if sortField is null or empty
        if (sortField == null || sortField.trim().isEmpty()) {
            return "name";
        }
        
        // Check if the requested sort field is allowed
        String sanitized = sortField.trim().toLowerCase();
        if (allowedFields.contains(sanitized)) {
            return sanitized;
        }
        
        // Map some common alternative names to the actual field names
        if ("price".equals(sanitized)) {
            return "pricePerItem";
        }
        if ("quantity".equals(sanitized)) {
            return "packets";
        }
        
        // Default to "name" if the requested field is not allowed
        return "name";
    }
    
    /**
     * Map a Product entity to a PublicProductDto
     */
    private PublicProductDto mapToPublicProductDto(Product product) {
        // Get the business
        Business business = product.getBusiness();
        
        // Get image URLs
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        
        // Calculate total price and quantity
        double totalPrice = product.getPricePerItem() * product.getPackets() * product.getItemsPerPacket();
        int totalQuantity = product.getPackets() * product.getItemsPerPacket();
        
        // Calculate packets available
        int fullPackets = product.getPackets();
        int additionalUnits = 0;
        int itemsPerPacket = product.getItemsPerPacket();
        
        // Calculate packet price
        BigDecimal unitPrice = new BigDecimal(product.getPricePerItem());
        BigDecimal packetPrice = unitPrice.multiply(new BigDecimal(itemsPerPacket));
        BigDecimal fulfillmentCost = new BigDecimal(product.getFulfillmentCost());
        
        // Build and return the DTO
        return PublicProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(new BigDecimal(totalPrice))
                .quantity(totalQuantity)
                .imageUrls(imageUrls)
                .businessId(business.getId())
                .businessName(business.getName())
                .fullPacketsAvailable(fullPackets)
                .additionalUnits(additionalUnits)
                .itemsPerPacket(itemsPerPacket)
                .unitPrice(unitPrice)
                .fulfillmentCost(fulfillmentCost)
                .packetPrice(packetPrice)
                .build();
    }
}
