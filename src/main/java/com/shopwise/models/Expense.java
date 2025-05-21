package com.shopwise.models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private double amount;

    private String category;

    private String note;

    private java.time.LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private Business business;

}
