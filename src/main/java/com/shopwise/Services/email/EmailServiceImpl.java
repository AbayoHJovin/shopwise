package com.shopwise.Services.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the EmailService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:noreply@shopwise.com}")
    private String fromEmail;
    
    @Value("${app.name:ShopWise}")
    private String appName;

    /**
     * Send a simple text email asynchronously
     */
    @Override
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending simple email to: {}", to);
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);
                mailSender.send(message);
                log.info("Simple email sent successfully to: {}", to);
            } catch (Exception e) {
                log.error("Failed to send simple email to: {}", to, e);
                throw new RuntimeException("Failed to send email", e);
            }
        });
    }

    /**
     * Send an HTML email asynchronously
     */
    @Override
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending HTML email to: {}", to);
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);
                
                mailSender.send(message);
                log.info("HTML email sent successfully to: {}", to);
            } catch (MessagingException e) {
                log.error("Failed to send HTML email to: {}", to, e);
                throw new RuntimeException("Failed to send email", e);
            }
        });
    }

    /**
     * Send a password reset email with a reset link asynchronously
     */
    @Override
    @Async
    public void sendPasswordResetEmail(String to, String resetLink, String userName) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Sending password reset email to: {}", to);
                
                // Create HTML content using Thymeleaf template
                Context context = new Context();
                context.setVariable("resetLink", resetLink);
                context.setVariable("userName", userName != null ? userName : "User");
                context.setVariable("appName", appName);
                context.setVariable("expiryHours", 0.5); // 30 minutes
                
                String htmlContent = templateEngine.process("email/password-reset", context);
                
                // If template processing fails, use a simple HTML template
                if (htmlContent == null || htmlContent.isEmpty()) {
                    htmlContent = getDefaultPasswordResetTemplate(resetLink, userName);
                }
                
                // Send the email
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(appName + " - Password Reset Request");
                helper.setText(htmlContent, true);
                
                mailSender.send(message);
                log.info("Password reset email sent successfully to: {}", to);
            } catch (Exception e) {
                log.error("Failed to send password reset email to: {}", to, e);
                // Don't throw exception here to prevent user flow interruption
            }
        });
    }
    
    /**
     * Get a default HTML template for password reset emails
     * This is used as a fallback if Thymeleaf template processing fails
     */
    private String getDefaultPasswordResetTemplate(String resetLink, String userName) {
        String name = userName != null ? userName : "User";
        
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "    <meta charset=\"UTF-8\">"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "    <title>Password Reset</title>"
                + "    <style>"
                + "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }"
                + "        .container { border: 1px solid #ddd; border-radius: 5px; padding: 20px; }"
                + "        .header { text-align: center; margin-bottom: 20px; }"
                + "        .button { display: inline-block; background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }"
                + "        .footer { margin-top: 30px; font-size: 12px; color: #777; text-align: center; }"
                + "    </style>"
                + "</head>"
                + "<body>"
                + "    <div class=\"container\">"
                + "        <div class=\"header\">"
                + "            <h2>" + appName + " - Password Reset</h2>"
                + "        </div>"
                + "        <p>Hello " + name + ",</p>"
                + "        <p>We received a request to reset your password. Click the button below to reset it:</p>"
                + "        <p style=\"text-align: center;\">"
                + "            <a class=\"button\" href=\"" + resetLink + "\">Reset Password</a>"
                + "        </p>"
                + "        <p>This link will expire in 30 minutes.</p>"
                + "        <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>"
                + "        <p>Thank you,<br>The " + appName + " Team</p>"
                + "        <div class=\"footer\">"
                + "            <p>This is an automated email, please do not reply.</p>"
                + "        </div>"
                + "    </div>"
                + "</body>"
                + "</html>";
    }
}
