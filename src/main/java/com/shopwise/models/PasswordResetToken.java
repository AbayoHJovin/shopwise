package com.shopwise.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    private String email;
    
    private String token;
    
    private LocalDateTime expirationTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
