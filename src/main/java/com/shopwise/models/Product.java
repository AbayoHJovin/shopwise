package com.shopwise.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name="description", columnDefinition = "TEXT")
    private String description;
    private int packets;
    private int itemsPerPacket;
    private double pricePerItem;
    private double fulfillmentCost;

    @ManyToOne
    private Business business;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();
    
    /**
     * Adds an image to the product, enforcing the maximum of 3 images
     * @param image The image to add
     * @return true if the image was added, false if the maximum was reached
     */
    public boolean addImage(ProductImage image) {
        if (images.size() >= 3) {
            return false; // Maximum number of images reached
        }
        
        image.setProduct(this);
        return images.add(image);
    }
    
    /**
     * Removes an image from the product
     * @param image The image to remove
     * @return true if the image was removed
     */
    public boolean removeImage(ProductImage image) {
        return images.remove(image);
    }

}
