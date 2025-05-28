package com.shopwise.Dto.payment;

import com.shopwise.enums.SubscriptionPlan;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequestDto {
    
    @NotBlank(message = "Sender name is required")
    private String senderName;
    
    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount paid must be greater than 0")
    private BigDecimal amountPaid;
    
    private String comment;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;
}
