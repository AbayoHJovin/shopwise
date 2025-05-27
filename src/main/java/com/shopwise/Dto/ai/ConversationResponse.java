package com.shopwise.Dto.ai;

import com.shopwise.models.ai.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for conversation responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private UUID id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ConversationStatus status;
    private List<MessageDto> messages;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageDto {
        private UUID id;
        private String content;
        private LocalDateTime timestamp;
        private String type; // "user" or "ai"
        private Integer sequenceOrder;
        private String modelUsed; // only for AI responses
    }
}
