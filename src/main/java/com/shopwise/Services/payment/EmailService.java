package com.shopwise.Services.payment;

import com.shopwise.models.ManualPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service for sending email notifications related to payments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private static final String ADMIN_EMAIL = "abayohirwajovin@gmail.com";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("en", "RW"));
    
    /**
     * Send a notification email to the admin about a new payment request
     * 
     * @param paymentRequest The payment request details
     */
    @Async
    public void sendPaymentRequestNotification(ManualPaymentRequest paymentRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(ADMIN_EMAIL);
            helper.setSubject("New Payment Request - ShopWise");
            
            String formattedAmount = CURRENCY_FORMATTER.format(paymentRequest.getAmountPaid());
            String formattedDate = paymentRequest.getSubmittedAt().format(DATE_FORMATTER);
            
            String emailContent = buildPaymentRequestEmailContent(
                    paymentRequest.getSubmittedBy().getName(),
                    paymentRequest.getSubmittedBy().getEmail(),
                    paymentRequest.getSenderName(),
                    formattedAmount,
                    formattedDate,
                    paymentRequest.getId().toString(),
                    paymentRequest.getComment()
            );
            
            helper.setText(emailContent, true);
            mailSender.send(message);
            
            log.info("Payment request notification email sent to admin for request ID: {}", paymentRequest.getId());
        } catch (MessagingException e) {
            log.error("Failed to send payment request notification email", e);
            // Don't throw exception to avoid disrupting the main workflow
        }
    }
    
    /**
     * Send a notification email to the user about their payment status update
     * 
     * @param paymentRequest The payment request with updated status
     */
    @Async
    public void sendPaymentStatusUpdateNotification(ManualPaymentRequest paymentRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(paymentRequest.getSubmittedBy().getEmail());
            helper.setSubject("Payment Request Status Update - ShopWise");
            
            String formattedAmount = CURRENCY_FORMATTER.format(paymentRequest.getAmountPaid());
            String statusText = paymentRequest.getStatus().toString();
            
            String emailContent = buildStatusUpdateEmailContent(
                    paymentRequest.getSubmittedBy().getName(),
                    statusText,
                    formattedAmount,
                    paymentRequest.getComment()
            );
            
            helper.setText(emailContent, true);
            mailSender.send(message);
            
            log.info("Payment status update email sent to user for request ID: {}", paymentRequest.getId());
        } catch (MessagingException e) {
            log.error("Failed to send payment status update email", e);
            // Don't throw exception to avoid disrupting the main workflow
        }
    }
    
    /**
     * Build the HTML content for the payment request notification email
     */
    private String buildPaymentRequestEmailContent(String userName, String userEmail, String senderName, 
                                                 String amount, String date, String requestId, String comment) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #4a6ee0;'>New Payment Request</h2>" +
                "<p>A new payment request has been submitted in the ShopWise system.</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>User:</strong> " + userName + " (" + userEmail + ")</p>" +
                "<p><strong>Sender Name:</strong> " + senderName + "</p>" +
                "<p><strong>Amount:</strong> " + amount + "</p>" +
                "<p><strong>Submitted At:</strong> " + date + "</p>" +
                "<p><strong>Request ID:</strong> " + requestId + "</p>" +
                (comment != null && !comment.isEmpty() ? "<p><strong>Comment:</strong> " + comment + "</p>" : "") +
                "</div>" +
                "<p>Please log in to the admin dashboard to review and process this request.</p>" +
                "<p>Thank you,<br>ShopWise Team</p>" +
                "</div>";
    }
    
    /**
     * Build the HTML content for the payment status update email
     */
    private String buildStatusUpdateEmailContent(String userName, String status, String amount, String comment) {
        String statusColor = "gray";
        if (status.equals("APPROVED")) {
            statusColor = "green";
        } else if (status.equals("REJECTED")) {
            statusColor = "red";
        }
        
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #4a6ee0;'>Payment Request Update</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Your payment request has been reviewed and the status has been updated.</p>" +
                "<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>Status:</strong> <span style='color: " + statusColor + ";'>" + status + "</span></p>" +
                "<p><strong>Amount:</strong> " + amount + "</p>" +
                (comment != null && !comment.isEmpty() ? "<p><strong>Comment:</strong> " + comment + "</p>" : "") +
                "</div>" +
                "<p>If you have any questions, please contact our support team.</p>" +
                "<p>Thank you for using ShopWise!</p>" +
                "</div>";
    }
}
