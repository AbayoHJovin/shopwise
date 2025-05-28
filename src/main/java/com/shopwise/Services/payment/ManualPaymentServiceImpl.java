package com.shopwise.Services.payment;

import com.shopwise.Dto.payment.CreatePaymentRequestDto;
import com.shopwise.Dto.payment.ManualPaymentRequestDto;
import com.shopwise.Dto.payment.UpdatePaymentStatusDto;
import com.shopwise.Repository.ManualPaymentRequestRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.enums.PaymentStatus;
import com.shopwise.enums.SubscriptionPlan;
import com.shopwise.models.ManualPaymentRequest;
import com.shopwise.models.SubscriptionInfo;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the ManualPaymentService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManualPaymentServiceImpl implements ManualPaymentService {

    private final ManualPaymentRequestRepository paymentRequestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public ManualPaymentRequestDto createPaymentRequest(CreatePaymentRequestDto requestDto, User currentUser, String screenshotFileName) {
        // Validate inputs
        if (currentUser == null) {
            throw PaymentException.badRequest("User is required");
        }
        
        if (screenshotFileName == null || screenshotFileName.isEmpty()) {
            throw PaymentException.badRequest("Payment screenshot is required");
        }
        
        // Create new payment request
        ManualPaymentRequest paymentRequest = new ManualPaymentRequest();
        paymentRequest.setSenderName(requestDto.getSenderName());
        paymentRequest.setAmountPaid(requestDto.getAmountPaid());
        paymentRequest.setComment(requestDto.getComment());
        paymentRequest.setSubmittedAt(LocalDateTime.now());
        paymentRequest.setScreenshotFileName(screenshotFileName);
        paymentRequest.setStatus(PaymentStatus.PENDING);
        paymentRequest.setSubmittedBy(currentUser);
        
        // Save payment request
        ManualPaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
        
        // Send notification email to admin
        emailService.sendPaymentRequestNotification(savedRequest);
        
        // Log in daily summary
        try {
            // Get the first business from the user's businesses (if any)
            if (currentUser.getBusinesses() != null && !currentUser.getBusinesses().isEmpty()) {
                UUID businessId = currentUser.getBusinesses().get(0).getId();
                String planName = requestDto.getSubscriptionPlan().toString().replace("_", " ");
                dailySummaryService.logDailyAction(
                        businessId,
                        "Payment request submitted by " + currentUser.getName() + 
                        " for " + planName + " subscription plan. Amount: " + requestDto.getAmountPaid()
                );
            }
        } catch (Exception e) {
            log.error("Failed to log payment request in daily summary", e);
            // Don't throw exception to avoid disrupting the main workflow
        }
        
        return mapToDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentRequestDto> getAllPaymentRequests(PaymentStatus status, UUID userId) {
        List<ManualPaymentRequest> requests;
        
        if (status != null && userId != null) {
            // Filter by both status and user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> PaymentException.notFound("User not found with ID: " + userId));
            requests = paymentRequestRepository.findBySubmittedByAndStatus(user, status);
        } else if (status != null) {
            // Filter by status only
            requests = paymentRequestRepository.findByStatus(status);
        } else if (userId != null) {
            // Filter by user only
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> PaymentException.notFound("User not found with ID: " + userId));
            requests = paymentRequestRepository.findBySubmittedBy(user);
        } else {
            // No filters, get all
            requests = paymentRequestRepository.findAll();
        }
        
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ManualPaymentRequestDto getPaymentRequestById(UUID requestId) {
        ManualPaymentRequest request = paymentRequestRepository.findById(requestId)
                .orElseThrow(() -> PaymentException.notFound("Payment request not found with ID: " + requestId));
        
        return mapToDto(request);
    }

    @Override
    @Transactional
    public ManualPaymentRequestDto updatePaymentStatus(UUID requestId, UpdatePaymentStatusDto updateDto, User adminUser) {
        // Validate admin user
        if (adminUser == null || adminUser.getRole() != User.UserRoles.ADMIN) {
            throw PaymentException.forbidden("Only administrators can update payment status");
        }
        
        // Find payment request
        ManualPaymentRequest request = paymentRequestRepository.findById(requestId)
                .orElseThrow(() -> PaymentException.notFound("Payment request not found with ID: " + requestId));
        
        // Don't update if already in the requested state
        if (request.getStatus() == updateDto.getStatus()) {
            return mapToDto(request);
        }
        
        // Update status
        PaymentStatus oldStatus = request.getStatus();
        request.setStatus(updateDto.getStatus());
        
        // Add admin comment if provided
        if (updateDto.getAdminComment() != null && !updateDto.getAdminComment().isEmpty()) {
            String updatedComment = request.getComment() != null && !request.getComment().isEmpty() 
                    ? request.getComment() + "\n\nAdmin comment: " + updateDto.getAdminComment()
                    : "Admin comment: " + updateDto.getAdminComment();
            request.setComment(updatedComment);
        }
        
        // If approved, update user's subscription
        if (updateDto.getStatus() == PaymentStatus.APPROVED) {
            updateUserSubscription(request);
        }
        
        // Save updated request
        ManualPaymentRequest updatedRequest = paymentRequestRepository.save(request);
        
        // Send notification email to user
        emailService.sendPaymentStatusUpdateNotification(updatedRequest);
        
        // Log in daily summary
        try {
            // Get the first business from the user's businesses (if any)
            User user = request.getSubmittedBy();
            if (user.getBusinesses() != null && !user.getBusinesses().isEmpty()) {
                UUID businessId = user.getBusinesses().get(0).getId();
                String statusChange = "Payment request status changed from " + oldStatus + " to " + updateDto.getStatus();
                dailySummaryService.logDailyAction(
                        businessId,
                        statusChange + " for user " + user.getName() + ". Amount: " + request.getAmountPaid()
                );
            }
        } catch (Exception e) {
            log.error("Failed to log payment status update in daily summary", e);
            // Don't throw exception to avoid disrupting the main workflow
        }
        
        return mapToDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManualPaymentRequestDto> getPaymentRequestsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> PaymentException.notFound("User not found with ID: " + userId));
        
        List<ManualPaymentRequest> requests = paymentRequestRepository.findBySubmittedBy(user);
        
        return requests.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Update the user's subscription based on an approved payment request
     * 
     * @param request The approved payment request
     */
    private void updateUserSubscription(ManualPaymentRequest request) {
        User user = request.getSubmittedBy();
        
        // Extract subscription plan from the payment amount and comment
        // This is a simplified approach - in a real system, you might have a more structured way
        // to determine which plan the user is subscribing to
        SubscriptionPlan plan = determineSubscriptionPlan(request);
        
        // Update user's subscription info
        SubscriptionInfo subscriptionInfo = user.getSubscriptionInfo();
        if (subscriptionInfo == null) {
            subscriptionInfo = new SubscriptionInfo();
            user.setSubscriptionInfo(subscriptionInfo);
        }
        
        // Set subscription details
        subscriptionInfo.setPlan(plan);
        subscriptionInfo.setSubscribedAt(LocalDateTime.now());
        
        // Mark free trial as finished if upgrading from basic plan
        if (subscriptionInfo.getPlan() != SubscriptionPlan.BASIC) {
            subscriptionInfo.setFinishedFreeTrial(true);
        }
        
        // Save updated user
        userRepository.save(user);
        
        log.info("Updated subscription for user ID: {} to plan: {}", user.getId(), plan);
    }
    
    /**
     * Determine the subscription plan based on the payment request
     * This is a simplified approach - in a real system, you might have a more structured way
     * to determine which plan the user is subscribing to
     * 
     * @param request The payment request
     * @return The determined subscription plan
     */
    private SubscriptionPlan determineSubscriptionPlan(ManualPaymentRequest request) {
        // This is a simplified approach - in a real implementation, you might have a more
        // structured way to determine the plan from the payment details
        
        // Check the comment for plan information
        String comment = request.getComment();
        if (comment != null) {
            comment = comment.toLowerCase();
            if (comment.contains("weekly") || comment.contains("week")) {
                return SubscriptionPlan.PRO_WEEKLY;
            } else if (comment.contains("monthly") || comment.contains("month")) {
                return SubscriptionPlan.PRO_MONTHLY;
            }
        }
        
        // If no plan info in comment, try to determine from amount
        // Assuming 2500 RWF for weekly and 9000 RWF for monthly
        if (request.getAmountPaid().doubleValue() >= 8000) {
            return SubscriptionPlan.PRO_MONTHLY;
        } else if (request.getAmountPaid().doubleValue() >= 2000) {
            return SubscriptionPlan.PRO_WEEKLY;
        }
        
        // Default to basic if can't determine
        return SubscriptionPlan.BASIC;
    }
    
    /**
     * Map a ManualPaymentRequest entity to a ManualPaymentRequestDto
     * 
     * @param request The entity to map
     * @return The mapped DTO
     */
    private ManualPaymentRequestDto mapToDto(ManualPaymentRequest request) {
        User submittedBy = request.getSubmittedBy();
        
        return ManualPaymentRequestDto.builder()
                .id(request.getId())
                .senderName(request.getSenderName())
                .amountPaid(request.getAmountPaid())
                .comment(request.getComment())
                .submittedAt(request.getSubmittedAt())
                .screenshotFileName(request.getScreenshotFileName())
                .status(request.getStatus())
                .submittedById(submittedBy.getId())
                .submittedByName(submittedBy.getName())
                .submittedByEmail(submittedBy.getEmail())
                .subscriptionPlan(determineSubscriptionPlan(request))
                .build();
    }
}
