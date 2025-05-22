package com.shopwise.Services.cloudinary;

import org.springframework.http.HttpStatus;

public class CloudinaryException extends RuntimeException {
    private final HttpStatus status;

    private CloudinaryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static CloudinaryException notFound(String message) {
        return new CloudinaryException(message, HttpStatus.NOT_FOUND);
    }

    public static CloudinaryException badRequest(String message) {
        return new CloudinaryException(message, HttpStatus.BAD_REQUEST);
    }

    public static CloudinaryException internalServerError(String message) {
        return new CloudinaryException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
