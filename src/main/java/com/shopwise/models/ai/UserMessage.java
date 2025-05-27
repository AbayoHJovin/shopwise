package com.shopwise.models.ai;

import com.shopwise.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a message sent by a user to the AI assistant
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
    
    @OneToOne(mappedBy = "userMessage", cascade = CascadeType.ALL, orphanRemoval = true)
    private AiResponse response;
    
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.SENT;
    
    private Integer sequenceOrder;
}
