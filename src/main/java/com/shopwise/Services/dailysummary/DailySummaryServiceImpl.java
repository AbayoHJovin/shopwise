package com.shopwise.Services.dailysummary;

import com.shopwise.Dto.dailysummary.DailySummaryResponse;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.DailySummaryRepository;
import com.shopwise.models.Business;
import com.shopwise.models.DailySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailySummaryServiceImpl implements DailySummaryService {

    private final DailySummaryRepository dailySummaryRepository;
    private final BusinessRepository businessRepository;

    @Override
    public List<DailySummaryResponse> getDailySummaries(UUID businessId) {
        // Validate business exists
        if (!businessRepository.existsById(businessId)) {
            throw DailySummaryException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all daily summaries for the business
        List<DailySummary> dailySummaries = dailySummaryRepository.findByBusinessId(businessId);
        
        // Map to response DTOs
        return dailySummaries.stream()
                .map(this::mapToDailySummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DailySummaryResponse getSummaryByDate(UUID businessId, LocalDate date) {
        // Validate business exists
        if (!businessRepository.existsById(businessId)) {
            throw DailySummaryException.notFound("Business not found with ID: " + businessId);
        }
        
        // Convert LocalDate to LocalDateTime range (start of day to end of day)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // Get summaries within date range
        List<DailySummary> summaries = dailySummaryRepository.findByBusinessIdAndDateRange(
                businessId, startOfDay, endOfDay);
        
        // If no summary exists for the date, throw exception
        if (summaries.isEmpty()) {
            throw DailySummaryException.notFound("No daily summary found for business ID: " + 
                    businessId + " on date: " + date);
        }
        
        // Return the first summary (should be the most recent due to ORDER BY in the query)
        return mapToDailySummaryResponse(summaries.get(0));
    }

    @Override
    @Transactional
    public void logDailyAction(UUID businessId, String description) {
        // Validate input
        if (description == null || description.trim().isEmpty()) {
            throw DailySummaryException.badRequest("Description cannot be empty");
        }
        
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> DailySummaryException.notFound("Business not found with ID: " + businessId));
        
        // Create new daily summary
        DailySummary dailySummary = new DailySummary();
        dailySummary.setDescription(description);
        dailySummary.setTimestamp(LocalDateTime.now());
        dailySummary.setBusiness(business);
        
        // Save daily summary
        dailySummaryRepository.save(dailySummary);
    }
    
    // Helper method to map DailySummary entity to DailySummaryResponse DTO
    private DailySummaryResponse mapToDailySummaryResponse(DailySummary dailySummary) {
        return DailySummaryResponse.builder()
                .id(dailySummary.getId())
                .description(dailySummary.getDescription())
                .timestamp(dailySummary.getTimestamp())
                .businessId(dailySummary.getBusiness().getId())
                .businessName(dailySummary.getBusiness().getName())
                .build();
    }
}
