package com.shopwise.Services.ai;

import com.shopwise.models.User;
import com.shopwise.models.ai.AiResponse;
import com.shopwise.models.ai.Conversation;
import com.shopwise.models.ai.UserMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing chat conversations with the Gemini AI
 */
public interface GeminiChatService {
    
    /**
     * Send a message to the AI and get a response
     * 
     * @param message The message text
     * @param conversationId The ID of the conversation (optional, if null a new conversation will be created)
     * @param user The user sending the message
     * @return The AI's response
     */
    AiResponse sendMessage(String message, UUID conversationId, User user);
    
    /**
     * Create a new conversation
     * 
     * @param title The title of the conversation
     * @param user The user creating the conversation
     * @return The created conversation
     */
    Conversation createConversation(String title, User user);
    
    /**
     * Get a conversation by ID
     * 
     * @param conversationId The ID of the conversation
     * @param user The user requesting the conversation
     * @return The conversation
     */
    Conversation getConversation(UUID conversationId, User user);
    
    /**
     * Get all conversations for a user
     * 
     * @param user The user to get conversations for
     * @return List of conversations
     */
    List<Conversation> getUserConversations(User user);
    
    /**
     * Get the messages in a conversation
     * 
     * @param conversationId The ID of the conversation
     * @param user The user requesting the messages
     * @return List of messages and responses in sequence order
     */
    Map<String, Object> getConversationMessages(UUID conversationId, User user);
    
    /**
     * Delete a conversation
     * 
     * @param conversationId The ID of the conversation to delete
     * @param user The user deleting the conversation
     * @return true if deleted successfully
     */
    boolean deleteConversation(UUID conversationId, User user);
    
    /**
     * Delete all conversations for a user
     * 
     * @param user The user deleting their conversations
     * @return The number of conversations deleted
     */
    int deleteAllUserConversations(User user);
    
    /**
     * Archive a conversation
     * 
     * @param conversationId The ID of the conversation to archive
     * @param user The user archiving the conversation
     * @return The archived conversation
     */
    Conversation archiveConversation(UUID conversationId, User user);
}
