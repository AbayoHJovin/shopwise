package com.shopwise.Services.business;

import com.shopwise.Dto.business.BusinessAvailabilityRequest;
import com.shopwise.Dto.business.BusinessAvailabilityResponse;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of the BusinessAvailabilityService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessAvailabilityServiceImpl implements BusinessAvailabilityService {

    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public BusinessAvailabilityResponse updateAvailability(UUID businessId, BusinessAvailabilityRequest request) {
        log.info("Updating availability for business ID: {}, setting open to: {}", businessId, request.isOpen());
        
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with ID: " + businessId));
        
        // Check if the status is actually changing
        boolean currentStatus = business.isOpen();
        boolean newStatus = request.isOpen();
        
        if (currentStatus == newStatus) {
            log.info("Business availability status unchanged for business: {}", business.getName());
            return mapToBusinessAvailabilityResponse(business);
        }
        
        // Update the availability status
        business.setOpen(newStatus);
        
        // Save the updated business
        Business updatedBusiness = businessRepository.save(business);
        log.info("Successfully updated availability for business: {}", updatedBusiness.getName());
        
        // Log the availability change in daily summary
        String statusMessage = newStatus ? "opened" : "closed";
        dailySummaryService.logDailyAction(businessId, 
                "Business '" + updatedBusiness.getName() + "' was marked as " + statusMessage);
        
        // Return the response
        return mapToBusinessAvailabilityResponse(updatedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessAvailabilityResponse getAvailability(UUID businessId) {
        log.info("Getting availability for business ID: {}", businessId);
        
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found with ID: " + businessId));
        
        // Return the response
        return mapToBusinessAvailabilityResponse(business);
    }
    
    /**
     * Maps a Business entity to a BusinessAvailabilityResponse DTO
     * 
     * @param business The business entity to map
     * @return The mapped response DTO
     */
    private BusinessAvailabilityResponse mapToBusinessAvailabilityResponse(Business business) {
        return BusinessAvailabilityResponse.builder()
                .businessId(business.getId())
                .businessName(business.getName())
                .open(business.isOpen())
                .build();
    }
}
