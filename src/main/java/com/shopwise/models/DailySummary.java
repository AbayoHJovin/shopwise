package com.shopwise.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "daily_summary")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailySummary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime timestamp;

    @ManyToOne
    private Business business;
}
