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
@AllArgsConstructor
@NoArgsConstructor
public class SaleRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private int packetsSold;
    private int piecesSold;
    private int totalPiecesSold; // Total quantity in individual pieces
    private LocalDateTime saleTime;
    private boolean manuallyAdjusted;
    private boolean loggedLater;
    private String notes;
    private LocalDateTime actualSaleTime;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Business business;
}
