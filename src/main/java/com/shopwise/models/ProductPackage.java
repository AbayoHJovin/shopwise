package com.shopwise.models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_packages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductPackage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label; // e.g., "Box of 12"

    private int quantity;

    private double purchaseCost;

    private double sellingPricePerUnit;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    private int soldUnits = 0;

}
