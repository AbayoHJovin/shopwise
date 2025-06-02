package com.shopwise.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "collaboration_requests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String email;
    private String token;
    private LocalDateTime expiryDate;

    @ManyToOne
    private Business business;
}
