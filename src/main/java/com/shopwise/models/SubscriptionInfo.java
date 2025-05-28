package com.shopwise.models;

import com.shopwise.enums.SubscriptionPlan;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionInfo {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan = SubscriptionPlan.BASIC;
    
    @Column(nullable = false)
    private boolean finishedFreeTrial = false;
    
    private LocalDateTime freeTrialStartedAt;
    
    private LocalDateTime subscribedAt;
    
    /**
     * Check if the subscription is active
     * 
     * @param currentTime The current time to check against
     * @return true if the subscription is active, false otherwise
     */
    public boolean isActive(LocalDateTime currentTime) {
        // Basic plan users get a 14-day free trial of premium features
        if (plan == SubscriptionPlan.BASIC) {
            if (finishedFreeTrial) {
                return true; // Basic plan with finished trial
            }
            
            // Check if free trial is still active
            if (freeTrialStartedAt != null) {
                LocalDateTime freeTrialEndDate = freeTrialStartedAt.plusDays(14);
                if (currentTime.isAfter(freeTrialEndDate)) {
                    // Free trial has expired
                    finishedFreeTrial = true;
                    return true; // Return to basic features
                }
                return true; // Free trial still active
            }
            return true; // Basic plan, trial not started yet
        }
        
        // Paid plans
        if (subscribedAt == null) {
            return false;
        }
        
        LocalDateTime expirationDate;
        if (plan == SubscriptionPlan.PRO_WEEKLY) {
            expirationDate = subscribedAt.plusWeeks(1);
        } else if (plan == SubscriptionPlan.PRO_MONTHLY) {
            expirationDate = subscribedAt.plusMonths(1);
        } else {
            return false;
        }
        
        return currentTime.isBefore(expirationDate);
    }
    
    /**
     * Get the expiration date of the current subscription
     * 
     * @return The expiration date or null if on basic plan
     */
    public LocalDateTime getExpirationDate() {
        // For free trial
        if (plan == SubscriptionPlan.BASIC && !finishedFreeTrial && freeTrialStartedAt != null) {
            return freeTrialStartedAt.plusDays(14);
        }
        
        // For paid plans
        if (plan == SubscriptionPlan.BASIC || subscribedAt == null) {
            return null;
        }
        
        if (plan == SubscriptionPlan.PRO_WEEKLY) {
            return subscribedAt.plusWeeks(1);
        } else if (plan == SubscriptionPlan.PRO_MONTHLY) {
            return subscribedAt.plusMonths(1);
        }
        
        return null;
    }
    
    /**
     * Get the remaining days in the subscription
     * 
     * @param currentTime The current time to check against
     * @return The number of days remaining or -1 if on basic plan
     */
    public long getRemainingDays(LocalDateTime currentTime) {
        // For free trial
        if (plan == SubscriptionPlan.BASIC && !finishedFreeTrial && freeTrialStartedAt != null) {
            LocalDateTime freeTrialEndDate = freeTrialStartedAt.plusDays(14);
            if (currentTime.isBefore(freeTrialEndDate)) {
                return java.time.Duration.between(currentTime, freeTrialEndDate).toDays();
            }
            return 0; // Trial expired
        }
        
        // For paid plans
        LocalDateTime expirationDate = getExpirationDate();
        if (expirationDate == null) {
            return -1; // Basic plan or invalid subscription
        }
        
        return java.time.Duration.between(currentTime, expirationDate).toDays();
    }
    
    /**
     * Start the free trial for this user
     * 
     * @param currentTime The current time when the trial is started
     * @return true if the trial was started, false if already started or finished
     */
    public boolean startFreeTrial(LocalDateTime currentTime) {
        if (finishedFreeTrial || freeTrialStartedAt != null) {
            return false; // Trial already started or finished
        }
        
        freeTrialStartedAt = currentTime;
        return true;
    }
    
    /**
     * Check if the user is currently in their free trial period
     * 
     * @param currentTime The current time to check against
     * @return true if the user is in their free trial period
     */
    public boolean isInFreeTrial(LocalDateTime currentTime) {
        if (plan != SubscriptionPlan.BASIC || finishedFreeTrial || freeTrialStartedAt == null) {
            return false;
        }
        
        LocalDateTime freeTrialEndDate = freeTrialStartedAt.plusDays(14);
        return currentTime.isBefore(freeTrialEndDate);
    }
    
    /**
     * Get the number of days remaining in the free trial
     * 
     * @param currentTime The current time to check against
     * @return The number of days remaining in the trial or 0 if not in trial
     */
    public long getFreeTrialRemainingDays(LocalDateTime currentTime) {
        if (!isInFreeTrial(currentTime)) {
            return 0;
        }
        
        LocalDateTime freeTrialEndDate = freeTrialStartedAt.plusDays(14);
        return java.time.Duration.between(currentTime, freeTrialEndDate).toDays();
    }
}
