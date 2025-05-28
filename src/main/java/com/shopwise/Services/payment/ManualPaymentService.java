package com.shopwise.Services.payment;

import com.shopwise.Dto.payment.CreatePaymentRequestDto;
import com.shopwise.Dto.payment.ManualPaymentRequestDto;
import com.shopwise.Dto.payment.UpdatePaymentStatusDto;
import com.shopwise.enums.PaymentStatus;
import com.shopwise.models.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for handling manual payment requests and subscription management
 */
public interface ManualPaymentService {
    
    /**
     * Create a new manual payment request
     * 
     * @param requestDto The payment request details
     * @param currentUser The user making the request
     * @param screenshotFileName The name of the uploaded screenshot file
     * @return The created payment request
     */
    ManualPaymentRequestDto createPaymentRequest(CreatePaymentRequestDto requestDto, User currentUser, String screenshotFileName);
    
    /**
     * Get all payment requests with optional filtering
     * 
     * @param status Optional status filter
     * @param userId Optional user ID filter
     * @return List of payment requests matching the filters
     */
    List<ManualPaymentRequestDto> getAllPaymentRequests(PaymentStatus status, UUID userId);
    
    /**
     * Get a specific payment request by ID
     * 
     * @param requestId The payment request ID
     * @return The payment request if found
     */
    ManualPaymentRequestDto getPaymentRequestById(UUID requestId);
    
    /**
     * Update the status of a payment request
     * 
     * @param requestId The payment request ID
     * @param updateDto The update details including new status
     * @param adminUser The admin user making the update
     * @return The updated payment request
     */
    ManualPaymentRequestDto updatePaymentStatus(UUID requestId, UpdatePaymentStatusDto updateDto, User adminUser);
    
    /**
     * Get all payment requests for a specific user
     * 
     * @param userId The user ID
     * @return List of payment requests for the user
     */
    List<ManualPaymentRequestDto> getPaymentRequestsByUser(UUID userId);
}
