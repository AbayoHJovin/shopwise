package com.shopwise.Services.availability;

import org.springframework.http.HttpStatus;

public class AvailabilitySlotException extends RuntimeException {
    private final HttpStatus status;

    public AvailabilitySlotException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static AvailabilitySlotException notFound(String message) {
        return new AvailabilitySlotException(message, HttpStatus.NOT_FOUND);
    }

    public static AvailabilitySlotException forbidden(String message) {
        return new AvailabilitySlotException(message, HttpStatus.FORBIDDEN);
    }

    public static AvailabilitySlotException badRequest(String message) {
        return new AvailabilitySlotException(message, HttpStatus.BAD_REQUEST);
    }

    public static AvailabilitySlotException conflict(String message) {
        return new AvailabilitySlotException(message, HttpStatus.CONFLICT);
    }
}
