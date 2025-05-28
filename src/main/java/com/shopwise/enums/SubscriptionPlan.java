package com.shopwise.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum representing the available subscription plans
 */
public enum SubscriptionPlan {
    @JsonProperty("BASIC")
    BASIC,           // Free plan
    
    @JsonProperty("PRO_WEEKLY")
    PRO_WEEKLY,      // Weekly premium plan (2500 RWF)
    
    @JsonProperty("PRO_MONTHLY")
    PRO_MONTHLY;     // Monthly premium plan (9000 RWF)
    
    /**
     * Custom deserializer to handle frontend values
     * 
     * @param value The string value to convert to enum
     * @return The matching enum value
     */
    @JsonCreator
    public static SubscriptionPlan fromString(String value) {
        if (value == null) {
            return null;
        }
        
        // Handle frontend values
        switch (value.toUpperCase()) {
            case "BASIC":
                return BASIC;
            case "WEEKLY":
            case "PRO_WEEKLY":
                return PRO_WEEKLY;
            case "MONTHLY":
            case "PRO_MONTHLY":
                return PRO_MONTHLY;
            default:
                throw new IllegalArgumentException("Unknown subscription plan: " + value);
        }
    }
}
