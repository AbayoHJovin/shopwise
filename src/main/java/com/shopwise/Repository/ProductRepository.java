package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByBusiness(Business business);
    List<Product> findByBusinessId(UUID businessId);
    
    /**
     * Find products by business ID with pagination
     * 
     * @param businessId The business ID
     * @param pageable Pagination information
     * @return Page of products
     */
    Page<Product> findByBusinessId(UUID businessId, Pageable pageable);
}
