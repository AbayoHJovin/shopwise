package com.shopwise.Services.salerecord;

import org.springframework.http.HttpStatus;

public class SaleRecordException extends RuntimeException {
    private final HttpStatus status;

    public SaleRecordException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static SaleRecordException notFound(String message) {
        return new SaleRecordException(message, HttpStatus.NOT_FOUND);
    }

    public static SaleRecordException forbidden(String message) {
        return new SaleRecordException(message, HttpStatus.FORBIDDEN);
    }

    public static SaleRecordException badRequest(String message) {
        return new SaleRecordException(message, HttpStatus.BAD_REQUEST);
    }
}
