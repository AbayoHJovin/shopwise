package com.shopwise.Repository;

import com.shopwise.enums.PaymentStatus;
import com.shopwise.models.ManualPaymentRequest;
import com.shopwise.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing ManualPaymentRequest entities
 */
@Repository
public interface ManualPaymentRequestRepository extends JpaRepository<ManualPaymentRequest, UUID> {
    
    /**
     * Find all payment requests submitted by a specific user
     * 
     * @param user The user who submitted the payment requests
     * @return List of payment requests
     */
    List<ManualPaymentRequest> findBySubmittedBy(User user);
    
    /**
     * Find all payment requests with a specific status
     * 
     * @param status The payment status to filter by
     * @return List of payment requests
     */
    List<ManualPaymentRequest> findByStatus(PaymentStatus status);
    
    /**
     * Find all payment requests submitted by a specific user with a specific status
     * 
     * @param user The user who submitted the payment requests
     * @param status The payment status to filter by
     * @return List of payment requests
     */
    List<ManualPaymentRequest> findBySubmittedByAndStatus(User user, PaymentStatus status);
}
