package com.shopwise.Dto.payment;

import com.shopwise.enums.PaymentStatus;
import com.shopwise.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualPaymentRequestDto {
    private UUID id;
    
    @NotBlank(message = "Sender name is required")
    private String senderName;
    
    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount paid must be greater than 0")
    private BigDecimal amountPaid;
    
    private String comment;
    
    private LocalDateTime submittedAt;
    
    private String screenshotFileName;
    
    private PaymentStatus status;
    
    private UUID submittedById;
    
    private String submittedByName;
    
    private String submittedByEmail;
    
    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan subscriptionPlan;
}
