package com.shopwise.Repository;

import com.shopwise.models.Business;
import com.shopwise.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByBusiness(Business business);
    List<Product> findByBusinessId(UUID businessId);
}
