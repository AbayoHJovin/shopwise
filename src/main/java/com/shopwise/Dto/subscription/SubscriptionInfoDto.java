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
                .isAllowedPremium(subscriptionInfo.isActive(now) || subscriptionInfo.isInFreeTrial(now))
                .remainingDays(subscriptionInfo.getRemainingDays(now))
                .isInFreeTrial(subscriptionInfo.isInFreeTrial(now))
                .build();
    }
}
