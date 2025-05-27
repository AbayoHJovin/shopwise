package com.shopwise.controllers.ai;

import com.shopwise.Dto.ApiResponse;
import com.shopwise.Services.UserService;
import com.shopwise.Services.ai.AIAnalyticsService;
import com.shopwise.Services.ai.GeminiChatService;
import com.shopwise.models.User;
import com.shopwise.models.ai.AiResponse;
import com.shopwise.models.ai.Conversation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Gemini AI chat functionality
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class GeminiChatController {

    private final GeminiChatService geminiChatService;
    private final UserService userService;
    private final AIAnalyticsService aiAnalyticsService;

    /**
     * Send a message to the AI and get a response
     *
     * @param requestBody The request containing the message and optional conversation ID
     * @param authentication The authenticated user
     * @return The AI's response
     */
    @PostMapping("/chat")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> requestBody, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract message from request
            String message = (String) requestBody.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Message cannot be empty"));
            }
            
            // Extract conversation ID if provided
            UUID conversationId = null;
            if (requestBody.containsKey("conversationId") && requestBody.get("conversationId") != null) {
                try {
                    conversationId = UUID.fromString(requestBody.get("conversationId").toString());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid conversation ID format"));
                }
            }
            
            // Send message to AI
            AiResponse aiResponse = geminiChatService.sendMessage(message, conversationId, currentUser);
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("conversationId", aiResponse.getConversation().getId());
            response.put("messageId", aiResponse.getUserMessage().getId());
            response.put("responseId", aiResponse.getId());
            response.put("content", aiResponse.getContent());
            response.put("timestamp", aiResponse.getTimestamp());
            response.put("modelUsed", aiResponse.getModelUsed());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error processing AI chat request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while processing your request"));
        }
    }

    /**
     * Get a conversation by ID
     *
     * @param conversationId The ID of the conversation
     * @param authentication The authenticated user
     * @return The conversation with messages
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<?> getConversation(@PathVariable UUID conversationId, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Get conversation messages
            Map<String, Object> conversation = geminiChatService.getConversationMessages(conversationId, currentUser);
            
            return ResponseEntity.ok(conversation);
        } catch (IllegalArgumentException e) {
            log.error("Invalid conversation ID", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving the conversation"));
        }
    }

    /**
     * Get all conversations for the authenticated user
     *
     * @param authentication The authenticated user
     * @return List of conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Get user conversations
            List<Conversation> conversations = geminiChatService.getUserConversations(currentUser);
            
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            log.error("Error retrieving user conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving conversations"));
        }
    }

    /**
     * Create a new conversation
     *
     * @param requestBody The request containing the conversation title
     * @param authentication The authenticated user
     * @return The created conversation
     */
    @PostMapping("/conversation")
    public ResponseEntity<?> createConversation(@RequestBody Map<String, String> requestBody, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract title from request
            String title = requestBody.get("title");
            if (title == null || title.trim().isEmpty()) {
                title = "New Conversation";
            }
            
            // Create conversation
            Conversation conversation = geminiChatService.createConversation(title, currentUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        } catch (Exception e) {
            log.error("Error creating conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while creating the conversation"));
        }
    }

    /**
     * Delete a conversation
     *
     * @param conversationId The ID of the conversation to delete
     * @param authentication The authenticated user
     * @return Success/failure response
     */
    @DeleteMapping("/conversation/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable UUID conversationId, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Delete conversation
            boolean deleted = geminiChatService.deleteConversation(conversationId, currentUser);
            
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Conversation deleted successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Failed to delete conversation"));
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid conversation ID", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while deleting the conversation"));
        }
    }

    /**
     * Delete all conversations for the authenticated user
     *
     * @param authentication The authenticated user
     * @return Success/failure response
     */
    @DeleteMapping("/conversations")
    public ResponseEntity<?> deleteAllConversations(Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Delete all conversations
            int count = geminiChatService.deleteAllUserConversations(currentUser);
            
            return ResponseEntity.ok(new ApiResponse(true, count + " conversations deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting conversations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while deleting conversations"));
        }
    }

    /**
     * Archive a conversation
     *
     * @param conversationId The ID of the conversation to archive
     * @param authentication The authenticated user
     * @return The archived conversation
     */
    @PutMapping("/conversation/{conversationId}/archive")
    public ResponseEntity<?> archiveConversation(@PathVariable UUID conversationId, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Archive conversation
            Conversation archivedConversation = geminiChatService.archiveConversation(conversationId, currentUser);
            
            return ResponseEntity.ok(archivedConversation);
        } catch (IllegalArgumentException e) {
            log.error("Invalid conversation ID", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error archiving conversation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while archiving the conversation"));
        }
    }
    
    /**
     * Generate a daily log summary for a business
     *
     * @param request The HTTP request containing cookies
     * @param authentication The authenticated user
     * @return The daily log summary
     */
    @GetMapping("/business/daily-summary")
    public ResponseEntity<?> getDailyLogSummary(HttpServletRequest request, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract business ID from cookies
            String businessIdStr = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // Check if business ID is available
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "No business selected. Please select a business first."));
            }
            
            // Parse business ID
            UUID businessId;
            try {
                businessId = UUID.fromString(businessIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid business ID format: " + businessIdStr));
            }
            
            // Generate daily log summary
            String summary = aiAnalyticsService.generateDailyLogSummary(businessId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("businessId", businessId);
            response.put("summary", summary);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid business ID", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating daily log summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while generating the daily log summary"));
        }
    }
    
    /**
     * Generate expense analytics for a business
     *
     * @param request The HTTP request containing cookies
     * @param authentication The authenticated user
     * @return The expense analytics
     */
    @GetMapping("/business/expense-analytics")
    public ResponseEntity<?> getExpenseAnalytics(HttpServletRequest request, Authentication authentication) {
        try {
            // Get the authenticated user
            User currentUser = (User) authentication.getPrincipal();
            
            // Extract business ID from cookies
            String businessIdStr = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // Check if business ID is available
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "No business selected. Please select a business first."));
            }
            
            // Parse business ID
            UUID businessId;
            try {
                businessId = UUID.fromString(businessIdStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid business ID format: " + businessIdStr));
            }
            
            // Generate expense analytics
            String analytics = aiAnalyticsService.generateExpenseAnalytics(businessId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("businessId", businessId);
            response.put("analytics", analytics);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid business ID", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (SecurityException e) {
            log.error("Security violation", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating expense analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while generating expense analytics"));
        }
    }
}
