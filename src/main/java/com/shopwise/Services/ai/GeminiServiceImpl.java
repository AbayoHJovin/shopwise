package com.shopwise.Services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the GeminiService interface using RestTemplate
 */
@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    Dotenv dotenv = Dotenv.configure().load();
    private final String apiKey;
    
    @Value("${gemini.model.name:gemini-2.0-flash}")
    private String modelName;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    public GeminiServiceImpl() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        Dotenv dotenv = Dotenv.configure().load();
        this.apiKey = dotenv.get("GEMINI_API_KEY");
    }

    /**
     * Send a message to the Gemini AI and get a response
     * 
     * @param message The message to send
     * @param conversationHistory Previous messages in the conversation (optional)
     * @return The AI's response
     */
    @Override
    public String generateResponse(String message, String conversationHistory) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Build request URL
            String url = String.format(GEMINI_API_URL, modelName, apiKey);
            
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // Add system instructions
            addSystemInstructions(requestBody);
            
            ArrayNode contents = requestBody.putArray("contents");
            
            // Add system context as the first message
            addModelMessage(contents, getShopWiseSystemContext());
            
            // Add conversation history if available
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                // Parse conversation history and add as separate messages
                String[] messages = conversationHistory.split("\\n");
                for (String line : messages) {
                    if (line.startsWith("User: ")) {
                        addUserMessage(contents, line.substring(6));
                    } else if (line.startsWith("AI: ")) {
                        addModelMessage(contents, line.substring(4));
                    }
                }
            }
            
            // Add the current user message
            addUserMessage(contents, message);
            
            // Create the HTTP entity with headers and body
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse the response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String responseText = extractResponseText(responseJson);
            
            long endTime = System.currentTimeMillis();
            log.info("Gemini API response generated in {} ms", (endTime - startTime));
            
            return responseText;
        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            return "I'm sorry, I'm having trouble connecting to my knowledge base right now. Please try again later.";
        } catch (Exception e) {
            log.error("Error generating response from Gemini API", e);
            return "I'm sorry, I'm having trouble processing your request right now. Please try again later.";
        }
    }

    /**
     * Send a message to the Gemini AI with additional parameters and get a response
     * 
     * @param message The message to send
     * @param conversationHistory Previous messages in the conversation (optional)
     * @param parameters Additional parameters for the API call
     * @return The AI's response along with metadata
     */
    @Override
    public Map<String, Object> generateResponseWithMetadata(String message, String conversationHistory, Map<String, Object> parameters) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            // Build request URL
            String url = String.format(GEMINI_API_URL, modelName, apiKey);
            
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // Add system instructions
            addSystemInstructions(requestBody);
            
            // Add generation config if provided
            if (parameters != null && parameters.containsKey("temperature")) {
                ObjectNode generationConfig = requestBody.putObject("generationConfig");
                generationConfig.put("temperature", (Double) parameters.getOrDefault("temperature", 0.7));
                generationConfig.put("topP", (Double) parameters.getOrDefault("topP", 0.95));
                generationConfig.put("topK", (Integer) parameters.getOrDefault("topK", 40));
                generationConfig.put("maxOutputTokens", (Integer) parameters.getOrDefault("maxOutputTokens", 2048));
            }
            
            ArrayNode contents = requestBody.putArray("contents");
            
            // Add system context as the first message
            addModelMessage(contents, getShopWiseSystemContext());
            
            // Add conversation history if available
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                // Parse conversation history and add as separate messages
                String[] messages = conversationHistory.split("\\n");
                for (String line : messages) {
                    if (line.startsWith("User: ")) {
                        addUserMessage(contents, line.substring(6));
                    } else if (line.startsWith("AI: ")) {
                        addModelMessage(contents, line.substring(4));
                    }
                }
            }
            
            // Add the current user message
            addUserMessage(contents, message);
            
            // Create the HTTP entity with headers and body
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            // Parse the response
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String responseText = extractResponseText(responseJson);
            
            // Store response metadata
            JsonNode usageMetadata = responseJson.path("usageMetadata");
            
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            // Populate result map
            result.put("content", responseText);
            result.put("modelUsed", modelName);
            result.put("processingTimeMs", processingTime);
            result.put("containsCode", responseText.contains("```"));
            
            // Extract token counts if available
            if (!usageMetadata.isMissingNode()) {
                result.put("promptTokenCount", usageMetadata.path("promptTokenCount").asInt());
                result.put("candidatesTokenCount", usageMetadata.path("candidatesTokenCount").asInt());
                result.put("totalTokenCount", usageMetadata.path("totalTokenCount").asInt());
            } else {
                // Estimate token count (rough approximation)
                int estimatedTokenCount = (int) (responseText.split("\\s+").length * 1.3);
                result.put("tokenCount", estimatedTokenCount);
            }
            
            // Store raw response for debugging if requested
            if (parameters != null && parameters.containsKey("includeRawResponse") && (Boolean) parameters.get("includeRawResponse")) {
                result.put("rawResponse", response.getBody());
            }
            
            log.info("Gemini API response generated in {} ms", processingTime);
            
            return result;
        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            String errorMessage;
            
            // Check if this is a rate limit error (429 Too Many Requests)
            if (e.getMessage() != null && e.getMessage().contains("429 Too Many Requests")) {
                errorMessage = "I'm sorry, we've reached our API rate limit. Please try again in a few minutes.";
                log.warn("Rate limit exceeded for Gemini API. Consider upgrading your API plan or implementing rate limiting.");
                result.put("rateLimited", true);
            } else {
                errorMessage = "I'm sorry, I'm having trouble connecting to my knowledge base right now. Please try again later.";
            }
            
            result.put("content", errorMessage);
            result.put("error", e.getMessage());
            result.put("processingTimeMs", System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Error generating response from Gemini API", e);
            result.put("content", "I'm sorry, I'm having trouble processing your request right now. Please try again later.");
            result.put("error", e.getMessage());
            result.put("processingTimeMs", System.currentTimeMillis() - startTime);
            return result;
        }
    }
    
    /**
     * Add a user message to the contents array
     * 
     * @param contents The contents array
     * @param message The user message
     */
    private void addUserMessage(ArrayNode contents, String message) {
        ObjectNode content = contents.addObject();
        content.put("role", "user");
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", message);
    }
    
    /**
     * Add a model message to the contents array
     * 
     * @param contents The contents array
     * @param message The model message
     */
    private void addModelMessage(ArrayNode contents, String message) {
        ObjectNode content = contents.addObject();
        content.put("role", "model");
        ArrayNode parts = content.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", message);
    }
    
    /**
     * Extract the response text from the Gemini API response
     * 
     * @param responseJson The response JSON
     * @return The extracted text
     */
    private String extractResponseText(JsonNode responseJson) {
        try {
            return responseJson
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text").asText();
        } catch (Exception e) {
            log.error("Error extracting response text", e);
            return "I'm sorry, I encountered an error processing the response.";
        }
    }
    
    /**
     * Add system instructions to the request body
     * 
     * @param requestBody The request body to add system instructions to
     */
    private void addSystemInstructions(ObjectNode requestBody) {
        ObjectNode systemInstructions = requestBody.putObject("systemInstruction");
        ArrayNode parts = systemInstructions.putArray("parts");
        ObjectNode part = parts.addObject();
        part.put("text", getSystemInstructionsText());
    }
    
    /**
     * Get the system instructions text
     * 
     * @return The system instructions text
     */
    private String getSystemInstructionsText() {
        return "You are ShopWiseAI, a helpful AI assistant for the ShopWise business management application. " +
               "ShopWise helps small business owners manage their inventory, sales, employees, and expenses. " +
               "Your role is to assist users with analyzing their business data, providing insights, and answering questions about their business. " +
               "You should be professional, helpful, and concise in your responses. " +
               "When users ask about their business data, provide specific insights based on the context provided. " +
               "If you don't have enough information, ask clarifying questions or suggest what data might be helpful. " +
               "Always maintain a friendly, supportive tone and focus on providing actionable business insights.";
    }
    
    /**
     * Get the ShopWise system context
     * 
     * @return The ShopWise system context
     */
    private String getShopWiseSystemContext() {
        return "I am ShopWiseAI, an AI assistant integrated into the ShopWise business management application. " +
               "ShopWise is a comprehensive platform designed to help small business owners manage their operations efficiently. " +
               "The application includes the following key features:\n" +
               "1. Inventory Management: Track products, stock levels, and pricing\n" +
               "2. Sales Tracking: Record and analyze sales data\n" +
               "3. Employee Management: Manage staff information and schedules\n" +
               "4. Expense Tracking: Monitor and categorize business expenses\n" +
               "5. Business Analytics: View dashboards with key performance metrics\n\n" +
               "I can help users by:\n" +
               "- Analyzing sales trends and patterns\n" +
               "- Identifying top-selling products\n" +
               "- Monitoring inventory levels and suggesting restocking\n" +
               "- Tracking employee performance\n" +
               "- Analyzing expense categories and suggesting cost-saving opportunities\n" +
               "- Providing daily summaries of business activities\n" +
               "- Answering questions about the business data\n\n" +
               "When responding to user queries, I'll incorporate relevant business data from the context provided.";
    }
}
