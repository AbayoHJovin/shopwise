package com.shopwise.Services.ai;

import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ai.AiResponseRepository;
import com.shopwise.Repository.ai.ConversationRepository;
import com.shopwise.Repository.ai.UserMessageRepository;
import com.shopwise.models.Business;
import com.shopwise.models.User;
import com.shopwise.models.ai.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of the GeminiChatService interface
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiChatServiceImpl implements GeminiChatService {

    private final ConversationRepository conversationRepository;
    private final UserMessageRepository userMessageRepository;
    private final AiResponseRepository aiResponseRepository;
    private final GeminiService geminiService;
    private final BusinessContextService businessContextService;
    private final BusinessRepository businessRepository;
    
    // Pattern to detect business data requests - expanded to catch more queries
    private static final Pattern BUSINESS_DATA_PATTERN = Pattern.compile(
            "(?i).*(analyze|show|get|provide|display|tell me|what|how|which|where|when|why|can you|could you|would you|do i|does|is|are|have|has|many|much|overview|summary|report|list|count|total|number|amount|value|status|health|check|give me|help me|find|search|look|track|monitor|review|compare|calculate|estimate|predict|forecast|recommend|suggest)\\s+.*\\b(products?|sales?|revenue|expenses?|dashboard|employees?|inventory|business|analytics|statistics|metrics|performance|profit|loss|stock|items?|goods|merchandise|staff|team|personnel|money|income|earnings|costs?|spending|finances?|financial|data|information|records?|transactions?|customers?|clients?|suppliers?|vendors?|orders?|purchases?|returns?|refunds?|discounts?|promotions?|marketing|advertising|campaigns?|budget|cash|flow|balance|sheet|statement|account|accounting|tax|taxes|salary|salaries|wage|wages|payroll|payment|payments|invoice|invoices|receipt|receipts|bill|bills|debt|debts|credit|loan|loans|asset|assets|liability|liabilities|equity|capital|investment|investments|shareholder|shareholders|partner|partners|owner|owners|manager|managers|management|director|directors|board|ceo|cfo|coo|president|vice|executive|chief|officer|department|departments|division|divisions|branch|branches|store|stores|shop|shops|outlet|outlets|location|locations|region|regions|territory|territories|market|markets|segment|segments|sector|sectors|industry|industries|competition|competitors?|rival|rivals|peer|peers|benchmark|benchmarks|standard|standards|goal|goals|target|targets|objective|objectives|strategy|strategies|plan|plans|planning|forecast|forecasts|projection|projections|trend|trends|growth|decline|increase|decrease|rise|fall|up|down|high|low|best|worst|top|bottom|average|mean|median|mode|range|variance|deviation|correlation|regression|analysis|report|reports|chart|charts|graph|graphs|table|tables|spreadsheet|spreadsheets|database|databases|query|queries|filter|filters|sort|sorts|group|groups|aggregate|aggregates|sum|count|average|min|max|first|last|rank|ranks|rating|ratings|score|scores|grade|grades|tier|tiers|level|levels|category|categories|class|classes|type|types|kind|kinds|brand|brands|make|makes|model|models|series|version|versions|edition|editions|release|releases|update|updates|upgrade|upgrades|feature|features|function|functions|capability|capabilities|capacity|capacities|limit|limits|threshold|thresholds|boundary|boundaries|constraint|constraints|requirement|requirements|specification|specifications|detail|details|attribute|attributes|property|properties|characteristic|characteristics|quality|qualities|quantity|quantities|dimension|dimensions|size|sizes|weight|weights|height|heights|width|widths|depth|depths|length|lengths|area|areas|volume|volumes|space|spaces|distance|distances|location|locations|position|positions|coordinate|coordinates|address|addresses|zip|postal|code|codes|city|cities|state|states|province|provinces|country|countries|region|regions|zone|zones|district|districts|territory|territories|area|areas|locale|locales|place|places|site|sites|venue|venues|facility|facilities|building|buildings|structure|structures|premise|premises|property|properties|real|estate|land|plot|plots|lot|lots|parcel|parcels|acre|acres|hectare|hectares|square|footage|meter|meters|foot|feet|inch|inches|centimeter|centimeters|millimeter|millimeters|kilometer|kilometers|mile|miles|yard|yards)\\b.*");
    
    // Pattern to extract business ID from message
    private static final Pattern BUSINESS_ID_PATTERN = Pattern.compile(
            "(?i).*\\bbusiness(?:\\s+id)?\\s*[:#]?\\s*([a-f0-9\\-]{36})\\b.*");

    /**
     * Send a message to the AI and get a response
     * 
     * @param message The message text
     * @param conversationId The ID of the conversation (optional, if null a new conversation will be created)
     * @param user The user sending the message
     * @return The AI's response
     */
    @Override
    @Transactional
    public AiResponse sendMessage(String message, UUID conversationId, User user) {
        return sendMessage(message, conversationId, user, null);
    }
    
    @Override
    @Transactional
    public AiResponse sendMessage(String message, UUID conversationId, User user, UUID providedBusinessId) {
        // Get or create conversation
        Conversation conversation;
        if (conversationId != null) {
            conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
            
            // Verify user has access to this conversation
            if (!conversation.getUser().getId().equals(user.getId())) {
                throw new SecurityException("User does not have access to this conversation");
            }
        } else {
            // Create a new conversation with a title derived from the message
            String title = generateConversationTitle(message);
            conversation = createConversation(title, user);
        }
        
        // Get the sequence numbers for the new message and response
        int messageSequence = conversation.getUserMessages().size() + 1;
        int responseSequence = conversation.getAiResponses().size() + 1;
        
        // Create and save user message
        UserMessage userMessage = new UserMessage();
        userMessage.setContent(message);
        userMessage.setUser(user);
        userMessage.setConversation(conversation);
        userMessage.setTimestamp(LocalDateTime.now());
        userMessage.setStatus(MessageStatus.SENT);
        userMessage.setSequenceOrder(messageSequence);
        userMessage = userMessageRepository.save(userMessage);
        
        // Build conversation history for context
        String conversationHistory = buildConversationHistory(conversation);
        
        // Always include business context for all queries to make responses more relevant
        String enhancedMessage = message;
        Map<String, Object> parameters = new HashMap<>();
        
        // First, use the provided business ID from cookies if available
        UUID businessId = providedBusinessId;
        
        // If no business ID provided, try to extract from message
        if (businessId == null) {
            businessId = extractBusinessId(message);
        }
        
        // If still no business ID, try to find one from conversation metadata
        if (businessId == null) {
            businessId = getBusinessIdFromConversation(conversation);
        }
        
        // If still no business ID, check if there's a business cookie in the conversation metadata
        if (businessId == null && conversation.getMetadata() != null && conversation.getMetadata().contains("selectedBusiness=")) {
            try {
                String metadata = conversation.getMetadata();
                int startIndex = metadata.indexOf("selectedBusiness=") + 17;
                int endIndex = metadata.indexOf(",", startIndex);
                if (endIndex == -1) endIndex = metadata.length();
                
                String businessIdStr = metadata.substring(startIndex, endIndex);
                businessId = UUID.fromString(businessIdStr);
            } catch (Exception e) {
                log.warn("Could not extract business ID from conversation metadata", e);
            }
        }
        
        // As a last resort, try to get a business from the repository
        if (businessId == null) {
            try {
                // Look for businesses associated with this user using the repository instead of the user object
                List<Business> userBusinesses = businessRepository.findBusinessesByCollaborator(user);
                if (userBusinesses != null && !userBusinesses.isEmpty()) {
                    // Use the first business in the user's list
                    businessId = userBusinesses.get(0).getId();
                    log.info("Using first business from user's businesses: {}", businessId);
                }
            } catch (Exception e) {
                log.warn("Could not get businesses for user", e);
            }
        }
        
        // If we have a business ID, get business context
        if (businessId != null) {
            try {
                String businessContext = businessContextService.getBusinessContext(businessId, user);
                
                // Always enhance the message with business context
                enhancedMessage = "I need to analyze the following business data:\n\n" + 
                                 businessContext + "\n\n" +
                                 "Based on this information, please answer the following question: " + message;
                
                // Update the conversation title to include business reference if it's a new conversation
                if (conversation.getUserMessages().size() <= 1 && !conversation.getTitle().contains("[Business:")) {
                    Business business = businessRepository.findById(businessId).orElse(null);
                    if (business != null) {
                        conversation.setTitle(conversation.getTitle() + " [Business: " + business.getName() + "]");
                        conversationRepository.save(conversation);
                    }
                }
                
                // Set temperature lower for analytical responses
                parameters.put("temperature", 0.3);
                parameters.put("topK", 20);
                parameters.put("maxOutputTokens", 1024);
            } catch (Exception e) {
                log.error("Error getting business context", e);
            }
        } else {
            log.warn("No business ID found for context enhancement. Using generic response.");
        }
        
        // Get AI response with metadata
        long startTime = System.currentTimeMillis();
        Map<String, Object> responseData = geminiService.generateResponseWithMetadata(
            enhancedMessage, 
            conversationHistory,
            parameters
        );
        
        // Create and save AI response
        AiResponse aiResponse = new AiResponse();
        aiResponse.setContent((String) responseData.get("content"));
        aiResponse.setUserMessage(userMessage);
        aiResponse.setConversation(conversation);
        aiResponse.setTimestamp(LocalDateTime.now());
        aiResponse.setStatus(MessageStatus.SENT);
        aiResponse.setModelUsed((String) responseData.getOrDefault("modelUsed", "gemini-2.0-flash"));
        aiResponse.setSequenceOrder(responseSequence);
        aiResponse.setProcessingTimeMs((Long) responseData.getOrDefault("processingTimeMs", System.currentTimeMillis() - startTime));
        aiResponse.setTokenCount((Integer) responseData.getOrDefault("tokenCount", 0));
        aiResponse.setContainsCode((Boolean) responseData.getOrDefault("containsCode", false));
        
        // Save metadata as JSON string if available
        if (responseData.containsKey("metadata")) {
            aiResponse.setMetadata(responseData.get("metadata").toString());
        }
        
        // Save the response
        aiResponse = aiResponseRepository.save(aiResponse);
        
        // Update conversation last updated time
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        
        return aiResponse;
    }

    /**
     * Create a new conversation
     * 
     * @param title The title of the conversation
     * @param user The user creating the conversation
     * @return The created conversation
     */
    @Override
    @Transactional
    public Conversation createConversation(String title, User user) {
        Conversation conversation = new Conversation();
        conversation.setTitle(title);
        conversation.setUser(user);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversation.setStatus(ConversationStatus.ACTIVE);
        
        return conversationRepository.save(conversation);
    }

    /**
     * Get a conversation by ID
     * 
     * @param conversationId The ID of the conversation
     * @param user The user requesting the conversation
     * @return The conversation
     */
    @Override
    @Transactional(readOnly = true)
    public Conversation getConversation(UUID conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        
        // Verify user has access to this conversation
        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not have access to this conversation");
        }
        
        return conversation;
    }

    /**
     * Get all conversations for a user
     * 
     * @param user The user to get conversations for
     * @return List of conversations
     */
    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findByUser(user);
    }

    /**
     * Get the messages in a conversation
     * 
     * @param conversationId The ID of the conversation
     * @param user The user requesting the messages
     * @return Map containing conversation details and messages
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getConversationMessages(UUID conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        
        // Get messages and responses in order
        List<UserMessage> userMessages = userMessageRepository.findByConversationOrderBySequenceOrderAsc(conversation);
        List<AiResponse> aiResponses = aiResponseRepository.findByConversationOrderBySequenceOrderAsc(conversation);
        
        // Combine messages and responses into a chronological list
        List<Map<String, Object>> messages = new ArrayList<>();
        
        for (UserMessage userMessage : userMessages) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("id", userMessage.getId());
            messageData.put("content", userMessage.getContent());
            messageData.put("timestamp", userMessage.getTimestamp());
            messageData.put("type", "user");
            messageData.put("sequenceOrder", userMessage.getSequenceOrder());
            messages.add(messageData);
            
            // Find corresponding AI response
            Optional<AiResponse> response = aiResponses.stream()
                .filter(r -> r.getUserMessage().getId().equals(userMessage.getId()))
                .findFirst();
            
            if (response.isPresent()) {
                AiResponse aiResponse = response.get();
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("id", aiResponse.getId());
                responseData.put("content", aiResponse.getContent());
                responseData.put("timestamp", aiResponse.getTimestamp());
                responseData.put("type", "ai");
                responseData.put("sequenceOrder", aiResponse.getSequenceOrder());
                responseData.put("modelUsed", aiResponse.getModelUsed());
                messages.add(responseData);
            }
        }
        
        // Sort by sequence order
        messages.sort(Comparator.comparing(m -> (Integer) m.get("sequenceOrder")));
        
        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversation.getId());
        result.put("title", conversation.getTitle());
        result.put("createdAt", conversation.getCreatedAt());
        result.put("updatedAt", conversation.getUpdatedAt());
        result.put("status", conversation.getStatus());
        result.put("messages", messages);
        
        return result;
    }

    /**
     * Delete a conversation
     * 
     * @param conversationId The ID of the conversation to delete
     * @param user The user deleting the conversation
     * @return true if deleted successfully
     */
    @Override
    @Transactional
    public boolean deleteConversation(UUID conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        
        // Delete the conversation (cascade will delete messages and responses)
        conversationRepository.delete(conversation);
        
        return true;
    }

    /**
     * Delete all conversations for a user
     * 
     * @param user The user deleting their conversations
     * @return The number of conversations deleted
     */
    @Override
    @Transactional
    public int deleteAllUserConversations(User user) {
        List<Conversation> conversations = conversationRepository.findByUser(user);
        int count = conversations.size();
        
        conversationRepository.deleteAll(conversations);
        
        return count;
    }

    /**
     * Archive a conversation
     * 
     * @param conversationId The ID of the conversation to archive
     * @param user The user archiving the conversation
     * @return The archived conversation
     */
    @Override
    @Transactional
    public Conversation archiveConversation(UUID conversationId, User user) {
        Conversation conversation = getConversation(conversationId, user);
        
        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversation.setUpdatedAt(LocalDateTime.now());
        
        return conversationRepository.save(conversation);
    }
    
    /**
     * Generate a title for a new conversation based on the first message
     * 
     * @param message The first message in the conversation
     * @return A generated title
     */
    private String generateConversationTitle(String message) {
        // Truncate message if it's too long
        String truncatedMessage = message.length() > 30 
            ? message.substring(0, 27) + "..." 
            : message;
            
        return "Chat: " + truncatedMessage;
    }
    
    /**
     * Build conversation history string for context
     * 
     * @param conversation The conversation to build history for
     * @return A string containing the conversation history
     */
    private String buildConversationHistory(Conversation conversation) {
        // Get the last few messages for context (limit to last 10 exchanges)
        List<UserMessage> userMessages = userMessageRepository.findByConversationOrderBySequenceOrderAsc(conversation);
        List<AiResponse> aiResponses = aiResponseRepository.findByConversationOrderBySequenceOrderAsc(conversation);
        
        // Only use the last 10 messages for context
        if (userMessages.size() > 10) {
            userMessages = userMessages.subList(userMessages.size() - 10, userMessages.size());
        }
        
        StringBuilder history = new StringBuilder();
        
        for (UserMessage userMessage : userMessages) {
            history.append("User: ").append(userMessage.getContent()).append("\n");
            
            // Find corresponding AI response
            Optional<AiResponse> response = aiResponses.stream()
                .filter(r -> r.getUserMessage().getId().equals(userMessage.getId()))
                .findFirst();
                
            if (response.isPresent()) {
                history.append("AI: ").append(response.get().getContent()).append("\n");
            }
        }
        
        return history.toString();
    }
    
    /**
     * Check if a message is requesting business data
     * 
     * @param message The message to check
     * @return true if the message is requesting business data
     */
    private boolean isBusinessDataRequest(String message) {
        return BUSINESS_DATA_PATTERN.matcher(message).find();
    }
    
    /**
     * Extract business ID from a message
     * 
     * @param message The message to extract from
     * @return The extracted business ID, or null if none found
     */
    private UUID extractBusinessId(String message) {
        Matcher matcher = BUSINESS_ID_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group(1));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Get business ID from conversation title or previous messages
     * 
     * @param conversation The conversation to extract from
     * @return The business ID, or null if none found
     */
    private UUID getBusinessIdFromConversation(Conversation conversation) {
        // Check if the conversation title contains a business name
        String title = conversation.getTitle();
        if (title != null && title.contains("[Business: ")) {
            int startIndex = title.indexOf("[Business: ") + 11;
            int endIndex = title.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                String businessName = title.substring(startIndex, endIndex);
                // Look up the business by name
                List<Business> businesses = businessRepository.findByName(businessName);
                if (!businesses.isEmpty()) {
                    return businesses.get(0).getId();
                }
            }
        }
        
        // Check previous messages for business ID mentions
        List<UserMessage> userMessages = userMessageRepository.findByConversationOrderBySequenceOrderAsc(conversation);
        
        for (UserMessage message : userMessages) {
            UUID businessId = extractBusinessId(message.getContent());
            if (businessId != null) {
                return businessId;
            }
        }
        
        return null;
    }
}
