package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Find products by business ID and name containing the given string (case insensitive) with pagination
     * 
     * @param businessId The business ID
     * @param name The name to search for
     * @param pageable Pagination information
     * @return Page of products
     */
    Page<Product> findByBusinessIdAndNameContainingIgnoreCase(UUID businessId, String name, Pageable pageable);
    
    /**
     * Find products by name containing the given string (case insensitive)
     * 
     * @param name The product name to search for
     * @param pageable Pagination information
     * @return Page of products with names containing the given string
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find products by name containing the given string (case insensitive) for a specific business
     * 
     * @param name The product name to search for
     * @param businessId The business ID
     * @param pageable Pagination information
     * @return Page of products with names containing the given string for the specified business
     */
    Page<Product> findByNameContainingIgnoreCaseAndBusinessId(String name, UUID businessId, Pageable pageable);
    
    /**
     * Find businesses that have products with names containing the given string (case insensitive)
     * 
     * @param productName The product name to search for
     * @return List of business IDs that have products with names containing the given string
     */
    @Query("SELECT DISTINCT p.business.id FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :productName, '%'))")
    List<UUID> findBusinessIdsWithProductNameContaining(@Param("productName") String productName);
    
    /**
     * Count products by business ID
     * 
     * @param businessId The business ID
     * @return The number of products for the specified business
     */
    long countByBusinessId(UUID businessId);
}
