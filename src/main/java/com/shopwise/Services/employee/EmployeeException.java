package com.shopwise.Services.employee;

import org.springframework.http.HttpStatus;

public class EmployeeException extends RuntimeException {
    private final HttpStatus status;

    public EmployeeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static EmployeeException notFound(String message) {
        return new EmployeeException(message, HttpStatus.NOT_FOUND);
    }

    public static EmployeeException forbidden(String message) {
        return new EmployeeException(message, HttpStatus.FORBIDDEN);
    }

    public static EmployeeException badRequest(String message) {
        return new EmployeeException(message, HttpStatus.BAD_REQUEST);
    }
    
    public static EmployeeException conflict(String message) {
        return new EmployeeException(message, HttpStatus.CONFLICT);
    }
}
