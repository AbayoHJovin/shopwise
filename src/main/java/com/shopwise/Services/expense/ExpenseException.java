package com.shopwise.Services.expense;

import org.springframework.http.HttpStatus;

public class ExpenseException extends RuntimeException {
    private final HttpStatus status;

    public ExpenseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;  
    }

    public static ExpenseException notFound(String message) {
        return new ExpenseException(message, HttpStatus.NOT_FOUND);
    }

    public static ExpenseException forbidden(String message) {
        return new ExpenseException(message, HttpStatus.FORBIDDEN);
    }

    public static ExpenseException badRequest(String message) {
        return new ExpenseException(message, HttpStatus.BAD_REQUEST);
    }
}
