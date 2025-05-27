package com.shopwise.Services.ai;

import java.util.Map;

/**
 * Service for interacting with the Gemini AI API
 */
public interface GeminiService {
    
    /**
     * Send a message to the Gemini AI and get a response
     * 
     * @param message The message to send
     * @param conversationHistory Previous messages in the conversation (optional)
     * @return The AI's response
     */
    String generateResponse(String message, String conversationHistory);
    
    /**
     * Send a message to the Gemini AI with additional parameters and get a response
     * 
     * @param message The message to send
     * @param conversationHistory Previous messages in the conversation (optional)
     * @param parameters Additional parameters for the API call
     * @return The AI's response along with metadata
     */
    Map<String, Object> generateResponseWithMetadata(String message, String conversationHistory, Map<String, Object> parameters);
}
