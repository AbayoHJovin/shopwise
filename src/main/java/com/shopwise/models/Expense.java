package com.shopwise.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String title;

    private double amount;

    private String category;

    private String note;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;
}
