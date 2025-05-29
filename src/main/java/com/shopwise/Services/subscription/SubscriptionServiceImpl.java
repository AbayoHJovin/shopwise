package com.shopwise.Services.subscription;

import com.shopwise.Dto.subscription.SubscriptionInfoDto;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.SubscriptionInfo;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the SubscriptionService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final UserRepository userRepository;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public SubscriptionInfoDto startFreeTrial(UUID userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> SubscriptionException.notFound("User not found with ID: " + userId));
        
        // Check if the user is eligible for a free trial
        if (!isEligibleForFreeTrial(user)) {
            throw SubscriptionException.conflict("User has already used their free trial");
        }
        
        // Get or create subscription info
        SubscriptionInfo subscriptionInfo = user.getSubscriptionInfo();
        if (subscriptionInfo == null) {
            subscriptionInfo = new SubscriptionInfo();
            user.setSubscriptionInfo(subscriptionInfo);
        }
        
        // Start the free trial
        LocalDateTime now = LocalDateTime.now();
        subscriptionInfo.setFreeTrialStartedAt(now);
        
        // Save the user
        User savedUser = userRepository.save(user);
        
        // Log the action in daily summary if the user has a business
        if (user.getBusinesses() != null && !user.getBusinesses().isEmpty()) {
            try {
                UUID businessId = user.getBusinesses().get(0).getId();
                dailySummaryService.logDailyAction(
                        businessId,
                        "Free trial started by " + user.getName() + " (" + user.getEmail() + ")"
                );
            } catch (Exception e) {
                log.error("Failed to log free trial start in daily summary", e);
                // Don't throw exception to avoid disrupting the main workflow
            }
        }
        
        log.info("Free trial started for user: {}", userId);
        
        // Return the updated subscription info
        return SubscriptionInfoDto.fromEntity(savedUser.getSubscriptionInfo());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionInfoDto getSubscriptionInfo(UUID userId) {
        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> SubscriptionException.notFound("User not found with ID: " + userId));
        
        // Return the subscription info
        return SubscriptionInfoDto.fromEntity(user.getSubscriptionInfo());
    }

    @Override
    public boolean isEligibleForFreeTrial(User user) {
        if (user == null) {
            return false;
        }
        
        SubscriptionInfo subscriptionInfo = user.getSubscriptionInfo();
        
        // If no subscription info, user is eligible
        if (subscriptionInfo == null) {
            return true;
        }
        
        // If free trial already finished, not eligible
        if (subscriptionInfo.isFinishedFreeTrial()) {
            return false;
        }
        
        // If free trial already started, not eligible
        return subscriptionInfo.getFreeTrialStartedAt() == null;
    }
}
