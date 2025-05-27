package com.shopwise.Repository.ai;

import com.shopwise.models.ai.AiResponse;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing AiResponse entities
 */
@Repository
public interface AiResponseRepository extends JpaRepository<AiResponse, UUID> {
    
    /**
     * Find the response for a specific user message
     * 
     * @param userMessage The user message to find the response for
     * @return Optional containing the AI response, if any
     */
    Optional<AiResponse> findByUserMessage(UserMessage userMessage);
    
    /**
     * Find all responses for a specific conversation
     * 
     * @param conversation The conversation to find responses for
     * @return List of AI responses
     */
    List<AiResponse> findByConversation(Conversation conversation);
    
    /**
     * Find all responses for a specific conversation ordered by sequence
     * 
     * @param conversation The conversation to find responses for
     * @return List of AI responses ordered by sequence
     */
    List<AiResponse> findByConversationOrderBySequenceOrderAsc(Conversation conversation);
    
    /**
     * Find all responses for a specific conversation with pagination
     * 
     * @param conversation The conversation to find responses for
     * @param pageable Pagination information
     * @return Page of AI responses
     */
    Page<AiResponse> findByConversation(Conversation conversation, Pageable pageable);
    
    /**
     * Find all responses with a specific status
     * 
     * @param status The status to filter by
     * @return List of AI responses
     */
    List<AiResponse> findByStatus(MessageStatus status);
    
    /**
     * Find responses created between two dates
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of AI responses
     */
    List<AiResponse> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find responses that used a specific AI model
     * 
     * @param modelUsed The model to filter by
     * @return List of AI responses
     */
    List<AiResponse> findByModelUsed(String modelUsed);
    
    /**
     * Find the most recent response in a conversation
     * 
     * @param conversation The conversation to find the response for
     * @return The most recent AI response
     */
    AiResponse findFirstByConversationOrderByTimestampDesc(Conversation conversation);
    
    /**
     * Search for responses containing specific text
     * 
     * @param searchText The text to search for
     * @return List of matching AI responses
     */
    @Query("SELECT r FROM AiResponse r WHERE LOWER(r.content) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<AiResponse> searchByContent(String searchText);
    
    /**
     * Find responses with processing time greater than a threshold
     * 
     * @param threshold The processing time threshold in milliseconds
     * @return List of AI responses
     */
    List<AiResponse> findByProcessingTimeMsGreaterThan(Long threshold);
    
    /**
     * Find responses that contain code
     * 
     * @return List of AI responses containing code
     */
    List<AiResponse> findByContainsCodeTrue();
    
    /**
     * Count the number of responses in a conversation
     * 
     * @param conversation The conversation to count responses for
     * @return The number of responses
     */
    long countByConversation(Conversation conversation);
    
    /**
     * Calculate the average processing time for responses
     * 
     * @return The average processing time in milliseconds
     */
    @Query("SELECT AVG(r.processingTimeMs) FROM AiResponse r WHERE r.processingTimeMs IS NOT NULL")
    Double calculateAverageProcessingTime();
}
