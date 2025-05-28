package com.shopwise.Dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight DTO for conversation list items
 * Contains only essential information for displaying in a sidebar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationListItemDto {
    private UUID id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
