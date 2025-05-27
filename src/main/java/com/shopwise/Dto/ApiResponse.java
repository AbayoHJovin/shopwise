package com.shopwise.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response format for REST endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    
    // Optional data field that can be used to return additional information
    private Object data;
    
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
