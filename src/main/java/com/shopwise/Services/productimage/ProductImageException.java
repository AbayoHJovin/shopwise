package com.shopwise.Services.productimage;

import org.springframework.http.HttpStatus;

public class ProductImageException extends RuntimeException {
    private final HttpStatus status;

    private ProductImageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ProductImageException notFound(String message) {
        return new ProductImageException(message, HttpStatus.NOT_FOUND);
    }

    public static ProductImageException badRequest(String message) {
        return new ProductImageException(message, HttpStatus.BAD_REQUEST);
    }

    public static ProductImageException forbidden(String message) {
        return new ProductImageException(message, HttpStatus.FORBIDDEN);
    }
}
