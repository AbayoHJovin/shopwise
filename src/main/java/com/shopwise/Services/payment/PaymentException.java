package com.shopwise.Services.payment;

import org.springframework.http.HttpStatus;

/**
 * Custom exception for payment-related errors
 */
public class PaymentException extends RuntimeException {
    private final HttpStatus status;

    private PaymentException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static PaymentException badRequest(String message) {
        return new PaymentException(message, HttpStatus.BAD_REQUEST);
    }

    public static PaymentException notFound(String message) {
        return new PaymentException(message, HttpStatus.NOT_FOUND);
    }

    public static PaymentException forbidden(String message) {
        return new PaymentException(message, HttpStatus.FORBIDDEN);
    }

    public static PaymentException conflict(String message) {
        return new PaymentException(message, HttpStatus.CONFLICT);
    }

    public static PaymentException internalError(String message) {
        return new PaymentException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
