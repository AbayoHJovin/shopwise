package com.shopwise.Services.dailysummary;

import org.springframework.http.HttpStatus;

public class DailySummaryException extends RuntimeException {
    private final HttpStatus status;

    public DailySummaryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static DailySummaryException notFound(String message) {
        return new DailySummaryException(message, HttpStatus.NOT_FOUND);
    }

    public static DailySummaryException forbidden(String message) {
        return new DailySummaryException(message, HttpStatus.FORBIDDEN);
    }

    public static DailySummaryException badRequest(String message) {
        return new DailySummaryException(message, HttpStatus.BAD_REQUEST);
    }
}
