package com.shopwise.Repository;

import com.shopwise.models.Product;
import com.shopwise.models.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
    
    List<ProductImage> findByProduct(Product product);
    
    List<ProductImage> findByProductId(UUID productId);
    
    void deleteByProductId(UUID productId);
    
    void deleteByPublicId(String publicId);
}
