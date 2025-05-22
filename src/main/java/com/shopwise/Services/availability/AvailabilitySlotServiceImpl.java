package com.shopwise.Services.availability;

import com.shopwise.Dto.availability.AvailabilitySlotRequest;
import com.shopwise.Dto.availability.AvailabilitySlotResponse;
import com.shopwise.Repository.AvailabilitySlotRepository;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.AvailabilitySlot;
import com.shopwise.models.Business;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilitySlotServiceImpl implements AvailabilitySlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    @Transactional
    public AvailabilitySlotResponse addAvailability(UUID businessId, AvailabilitySlotRequest request) {
        // Validate business exists
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> AvailabilitySlotException.notFound("Business not found with ID: " + businessId));
        
        // Validate time range
        validateTimeRange(request.getStartTime(), request.getEndTime());
        
        // Check for overlapping slots
        List<AvailabilitySlot> overlappingSlots = availabilitySlotRepository.findOverlappingSlots(
                businessId, request.getStartTime(), request.getEndTime());
        
        if (!overlappingSlots.isEmpty()) {
            throw AvailabilitySlotException.conflict("The requested time slot overlaps with existing availability slots");
        }
        
        // Create new availability slot
        AvailabilitySlot availabilitySlot = new AvailabilitySlot();
        availabilitySlot.setStartTime(request.getStartTime());
        availabilitySlot.setEndTime(request.getEndTime());
        availabilitySlot.setNote(request.getNote());
        availabilitySlot.setBusiness(business);
        
        // Save availability slot
        AvailabilitySlot savedSlot = availabilitySlotRepository.save(availabilitySlot);
        
        // Log the availability slot creation in daily summary
        String formattedStartTime = request.getStartTime().format(DATE_TIME_FORMATTER);
        String formattedEndTime = request.getEndTime().format(DATE_TIME_FORMATTER);
        dailySummaryService.logDailyAction(businessId, 
                "Availability slot added from " + formattedStartTime + " to " + formattedEndTime);
        
        // Return response
        return mapToAvailabilitySlotResponse(savedSlot);
    }

    @Override
    public List<AvailabilitySlotResponse> getAvailability(UUID businessId) {
        // Validate business exists
        if (!businessRepository.existsById(businessId)) {
            throw AvailabilitySlotException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all availability slots for the business
        List<AvailabilitySlot> availabilitySlots = availabilitySlotRepository.findByBusinessId(businessId);
        
        // Map to response DTOs
        return availabilitySlots.stream()
                .map(this::mapToAvailabilitySlotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAvailability(UUID slotId) {
        // Find the availability slot
        AvailabilitySlot availabilitySlot = availabilitySlotRepository.findById(slotId)
                .orElseThrow(() -> AvailabilitySlotException.notFound("Availability slot not found with ID: " + slotId));
        
        // Get business ID and slot details for logging
        UUID businessId = availabilitySlot.getBusiness().getId();
        String formattedStartTime = availabilitySlot.getStartTime().format(DATE_TIME_FORMATTER);
        String formattedEndTime = availabilitySlot.getEndTime().format(DATE_TIME_FORMATTER);
        
        // Delete availability slot
        availabilitySlotRepository.delete(availabilitySlot);
        
        // Log the availability slot deletion in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Availability slot deleted from " + formattedStartTime + " to " + formattedEndTime);
    }
    
    // Helper methods
    
    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        // Check if start time is in the past
        if (startTime.isBefore(LocalDateTime.now())) {
            throw AvailabilitySlotException.badRequest("Start time cannot be in the past");
        }
        
        // Check if end time is after start time
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw AvailabilitySlotException.badRequest("End time must be after start time");
        }
        
        // Check if the duration is reasonable (e.g., not more than 24 hours)
        long hoursBetween = java.time.Duration.between(startTime, endTime).toHours();
        if (hoursBetween > 24) {
            throw AvailabilitySlotException.badRequest("Availability slot duration cannot exceed 24 hours");
        }
    }
    
    private AvailabilitySlotResponse mapToAvailabilitySlotResponse(AvailabilitySlot availabilitySlot) {
        return AvailabilitySlotResponse.builder()
                .id(availabilitySlot.getId())
                .startTime(availabilitySlot.getStartTime())
                .endTime(availabilitySlot.getEndTime())
                .note(availabilitySlot.getNote())
                .businessId(availabilitySlot.getBusiness().getId())
                .businessName(availabilitySlot.getBusiness().getName())
                .build();
    }
}
