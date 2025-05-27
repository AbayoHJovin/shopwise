package com.shopwise.models.ai;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a response from the AI assistant to a user message
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @OneToOne
    @JoinColumn(name = "user_message_id")
    private UserMessage userMessage;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;
    
    private String modelUsed = "gemini-2.0-flash";
    
    private Integer sequenceOrder;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    // Fields for analytics
    private Long processingTimeMs;
    private Integer tokenCount;
    private Boolean containsCode;
}
