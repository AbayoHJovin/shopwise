package com.shopwise.Services.discovery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for business discovery-related errors
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BusinessDiscoveryException extends RuntimeException {
    
    public BusinessDiscoveryException(String message) {
        super(message);
    }
    
    public BusinessDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
