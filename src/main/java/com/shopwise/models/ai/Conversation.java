package com.shopwise.models.ai;

import com.shopwise.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a conversation between a user and the AI assistant
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMessage> userMessages = new ArrayList<>();
    
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiResponse> aiResponses = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING)
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @Column(length = 2000)
    private String metadata;
    
    // Helper methods to maintain bidirectional relationship
    public void addUserMessage(UserMessage message) {
        userMessages.add(message);
        message.setConversation(this);
    }
    
    public void removeUserMessage(UserMessage message) {
        userMessages.remove(message);
        message.setConversation(null);
    }
    
    public void addAiResponse(AiResponse response) {
        aiResponses.add(response);
        response.setConversation(this);
    }
    
    public void removeAiResponse(AiResponse response) {
        aiResponses.remove(response);
        response.setConversation(null);
    }
}
