package com.shopwise.Services.email;

/**
 * Service interface for sending emails
 */
public interface EmailService {
    
    /**
     * Send a simple text email
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param text Email body text
     */
    void sendSimpleEmail(String to, String subject, String text);
    
    /**
     * Send an HTML email
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent Email body as HTML
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Send a password reset email with a reset link
     * 
     * @param to Recipient email address
     * @param resetLink Password reset link
     * @param userName User's name (if available)
     */
    void sendPasswordResetEmail(String to, String resetLink, String userName);
}
