package com.shopwise.Dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for chat message responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private UUID conversationId;
    private UUID messageId;
    private UUID responseId;
    private String content;
    private LocalDateTime timestamp;
    private String modelUsed;
    private Long processingTimeMs;
}
