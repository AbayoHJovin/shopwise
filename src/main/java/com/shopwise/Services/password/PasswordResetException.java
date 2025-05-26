package com.shopwise.Services.password;

import org.springframework.http.HttpStatus;

/**
 * Exception class for password reset operations
 */
public class PasswordResetException extends RuntimeException {
    
    private final HttpStatus status;
    
    private PasswordResetException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    /**
     * Create a not found exception (404)
     * 
     * @param message Error message
     * @return PasswordResetException with NOT_FOUND status
     */
    public static PasswordResetException notFound(String message) {
        return new PasswordResetException(message, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Create a bad request exception (400)
     * 
     * @param message Error message
     * @return PasswordResetException with BAD_REQUEST status
     */
    public static PasswordResetException badRequest(String message) {
        return new PasswordResetException(message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create an unauthorized exception (401)
     * 
     * @param message Error message
     * @return PasswordResetException with UNAUTHORIZED status
     */
    public static PasswordResetException unauthorized(String message) {
        return new PasswordResetException(message, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Create an internal server error exception (500)
     * 
     * @param message Error message
     * @return PasswordResetException with INTERNAL_SERVER_ERROR status
     */
    public static PasswordResetException serverError(String message) {
        return new PasswordResetException(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
