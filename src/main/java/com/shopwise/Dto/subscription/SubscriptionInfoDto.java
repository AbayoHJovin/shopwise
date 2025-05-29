package com.shopwise.Dto.subscription;

import com.shopwise.enums.SubscriptionPlan;
import com.shopwise.models.SubscriptionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInfoDto {
    private SubscriptionPlan plan;
    private boolean finishedFreeTrial;
    private LocalDateTime freeTrialStartedAt;
    private LocalDateTime subscribedAt;
    private LocalDateTime expirationDate;
    private boolean isAllowedPremium;
    private long remainingDays;
    private boolean isInFreeTrial;
    
    /**
     * Convert SubscriptionInfo entity to SubscriptionInfoDto
     * 
     * @param subscriptionInfo The SubscriptionInfo entity to convert
     * @return SubscriptionInfoDto representation of the subscription info
     */

    public static SubscriptionInfoDto fromEntity(SubscriptionInfo subscriptionInfo) {
        if (subscriptionInfo == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        return SubscriptionInfoDto.builder()
                .plan(subscriptionInfo.getPlan())
                .finishedFreeTrial(subscriptionInfo.isFinishedFreeTrial())
                .freeTrialStartedAt(subscriptionInfo.getFreeTrialStartedAt())
                .subscribedAt(subscriptionInfo.getSubscribedAt())
                .expirationDate(subscriptionInfo.getExpirationDate())
                .isAllowedPremium(isPremiumAllowed(subscriptionInfo, now))
                .remainingDays(subscriptionInfo.getRemainingDays(now))
                .isInFreeTrial(subscriptionInfo.isInFreeTrial(now))
                .build();
    }
    
    /**
     * Determine if the user is allowed to access premium features
     * 
     * @param subscriptionInfo The subscription info
     * @param currentTime The current time
     * @return true if the user is allowed to access premium features
     */
    private static boolean isPremiumAllowed(SubscriptionInfo subscriptionInfo, LocalDateTime currentTime) {
        // If no subscription info, no premium access
        if (subscriptionInfo == null) {
            return false;
        }
        
        // Check if user is in free trial
        if (subscriptionInfo.isInFreeTrial(currentTime)) {
            return true;
        }
        
        // Check if user has an active premium subscription
        if (subscriptionInfo.getPlan() != SubscriptionPlan.BASIC) {
            // For premium plans, check if subscription is still valid
            if (subscriptionInfo.getSubscribedAt() == null) {
                return false;
            }
            
            LocalDateTime expirationDate;
            if (subscriptionInfo.getPlan() == SubscriptionPlan.PRO_WEEKLY) {
                expirationDate = subscriptionInfo.getSubscribedAt().plusWeeks(1);
            } else if (subscriptionInfo.getPlan() == SubscriptionPlan.PRO_MONTHLY) {
                expirationDate = subscriptionInfo.getSubscribedAt().plusMonths(1);
            } else {
                return false;
            }
            
            return currentTime.isBefore(expirationDate);
        }
        
        // Basic plan with no free trial or expired free trial
        return false;
    }
}
