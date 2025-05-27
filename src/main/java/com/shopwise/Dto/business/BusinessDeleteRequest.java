package com.shopwise.Dto.business;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for business deletion requests
 * Contains the password of the user requesting the deletion for verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDeleteRequest {
    
    @NotBlank(message = "Password is required to confirm business deletion")
    private String password;
    
    private String confirmationText;
}
