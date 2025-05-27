package com.shopwise.Repository.ai;

import com.shopwise.models.User;
import com.shopwise.models.ai.Conversation;
import com.shopwise.models.ai.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Conversation entities
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    
    /**
     * Find all conversations for a specific user
     * 
     * @param user The user to find conversations for
     * @return List of conversations
     */
    List<Conversation> findByUser(User user);
    
    /**
     * Find all conversations for a specific user with pagination
     * 
     * @param user The user to find conversations for
     * @param pageable Pagination information
     * @return Page of conversations
     */
    Page<Conversation> findByUser(User user, Pageable pageable);
    
    /**
     * Find all conversations for a specific user with a specific status
     * 
     * @param user The user to find conversations for
     * @param status The status to filter by
     * @return List of conversations
     */
    List<Conversation> findByUserAndStatus(User user, ConversationStatus status);
    
    /**
     * Find all conversations for a specific user with a specific status and pagination
     * 
     * @param user The user to find conversations for
     * @param status The status to filter by
     * @param pageable Pagination information
     * @return Page of conversations
     */
    Page<Conversation> findByUserAndStatus(User user, ConversationStatus status, Pageable pageable);
    
    /**
     * Find the most recent conversation for a specific user
     * 
     * @param user The user to find the conversation for
     * @return Optional containing the most recent conversation, if any
     */
    Optional<Conversation> findFirstByUserOrderByUpdatedAtDesc(User user);
    
    /**
     * Find conversations created between two dates
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of conversations
     */
    List<Conversation> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Count the number of conversations for a specific user
     * 
     * @param user The user to count conversations for
     * @return The number of conversations
     */
    long countByUser(User user);
    
    /**
     * Count the number of conversations for a specific user with a specific status
     * 
     * @param user The user to count conversations for
     * @param status The status to filter by
     * @return The number of conversations
     */
    long countByUserAndStatus(User user, ConversationStatus status);
}
