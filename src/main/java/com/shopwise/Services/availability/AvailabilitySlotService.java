package com.shopwise.Services.availability;

import com.shopwise.Dto.availability.AvailabilitySlotRequest;
import com.shopwise.Dto.availability.AvailabilitySlotResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing business availability slots in the ShopWise system.
 */
public interface AvailabilitySlotService {

    /**
     * Adds a new availability slot for a business.
     * @param businessId UUID of the business
     * @param request Availability slot details
     * @return Created AvailabilitySlotResponse
     */
    AvailabilitySlotResponse addAvailability(UUID businessId, AvailabilitySlotRequest request);

    /**
     * Retrieves all availability slots for a specific business.
     * @param businessId UUID of the business
     * @return List of AvailabilitySlotResponse objects
     */
    List<AvailabilitySlotResponse> getAvailability(UUID businessId);

    /**
     * Deletes an availability slot.
     * @param slotId UUID of the availability slot to delete
     */
    void deleteAvailability(UUID slotId);
}
