package com.shopwise.Repository.ai;

import com.shopwise.models.User;
import com.shopwise.models.ai.Conversation;
import com.shopwise.models.ai.MessageStatus;
import com.shopwise.models.ai.UserMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing UserMessage entities
 */
@Repository
public interface UserMessageRepository extends JpaRepository<UserMessage, UUID> {
    
    /**
     * Find all messages for a specific conversation
     * 
     * @param conversation The conversation to find messages for
     * @return List of user messages
     */
    List<UserMessage> findByConversation(Conversation conversation);
    
    /**
     * Find all messages for a specific conversation ordered by sequence
     * 
     * @param conversation The conversation to find messages for
     * @return List of user messages ordered by sequence
     */
    List<UserMessage> findByConversationOrderBySequenceOrderAsc(Conversation conversation);
    
    /**
     * Find all messages for a specific conversation with pagination
     * 
     * @param conversation The conversation to find messages for
     * @param pageable Pagination information
     * @return Page of user messages
     */
    Page<UserMessage> findByConversation(Conversation conversation, Pageable pageable);
    
    /**
     * Find all messages from a specific user
     * 
     * @param user The user to find messages for
     * @return List of user messages
     */
    List<UserMessage> findByUser(User user);
    
    /**
     * Find all messages from a specific user with pagination
     * 
     * @param user The user to find messages for
     * @param pageable Pagination information
     * @return Page of user messages
     */
    Page<UserMessage> findByUser(User user, Pageable pageable);
    
    /**
     * Find all messages with a specific status
     * 
     * @param status The status to filter by
     * @return List of user messages
     */
    List<UserMessage> findByStatus(MessageStatus status);
    
    /**
     * Find messages created between two dates
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of user messages
     */
    List<UserMessage> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find the most recent message in a conversation
     * 
     * @param conversation The conversation to find the message for
     * @return The most recent user message
     */
    UserMessage findFirstByConversationOrderByTimestampDesc(Conversation conversation);
    
    /**
     * Search for messages containing specific text
     * 
     * @param searchText The text to search for
     * @return List of matching user messages
     */
    @Query("SELECT m FROM UserMessage m WHERE LOWER(m.content) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<UserMessage> searchByContent(String searchText);
    
    /**
     * Count the number of messages in a conversation
     * 
     * @param conversation The conversation to count messages for
     * @return The number of messages
     */
    long countByConversation(Conversation conversation);
    
    /**
     * Count the number of messages from a specific user
     * 
     * @param user The user to count messages for
     * @return The number of messages
     */
    long countByUser(User user);
}
