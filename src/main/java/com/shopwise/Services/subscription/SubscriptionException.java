package com.shopwise.Services.subscription;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for subscription-related errors
 */
public class SubscriptionException extends RuntimeException {
    private final HttpStatus status;

    private SubscriptionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static SubscriptionException badRequest(String message) {
        return new SubscriptionException(message, HttpStatus.BAD_REQUEST);
    }

    public static SubscriptionException notFound(String message) {
        return new SubscriptionException(message, HttpStatus.NOT_FOUND);
    }

    public static SubscriptionException forbidden(String message) {
        return new SubscriptionException(message, HttpStatus.FORBIDDEN);
    }

    public static SubscriptionException conflict(String message) {
        return new SubscriptionException(message, HttpStatus.CONFLICT);
    }

    public static SubscriptionException internalError(String message) {
        return new SubscriptionException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
