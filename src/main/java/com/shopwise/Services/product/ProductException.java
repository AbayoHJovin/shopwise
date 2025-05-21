package com.shopwise.Services.product;

import org.springframework.http.HttpStatus;

public class ProductException extends RuntimeException {
    private final HttpStatus status;

    public ProductException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ProductException notFound(String message) {
        return new ProductException(message, HttpStatus.NOT_FOUND);
    }

    public static ProductException forbidden(String message) {
        return new ProductException(message, HttpStatus.FORBIDDEN);
    }

    public static ProductException badRequest(String message) {
        return new ProductException(message, HttpStatus.BAD_REQUEST);
    }
}
