package com.shopwise.Dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for chat message requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private String message;
    private UUID conversationId;
}
