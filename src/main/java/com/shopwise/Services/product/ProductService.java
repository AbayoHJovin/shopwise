package com.shopwise.Services.product;

import com.shopwise.Dto.product.ProductRequest;
import com.shopwise.Dto.product.ProductUpdateRequest;
import com.shopwise.Dto.product.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    /**
     * Adds a new product to a business.
     * @param businessId UUID of the business.
     * @param request Product details including name, description, price etc.
     * @return Created ProductResponse
     */
    ProductResponse addProduct(UUID businessId, ProductRequest request);

    /**
     * Updates an existing product.
     * @param productId UUID of the product to update.
     * @param request Updated fields (name, price, stock, etc.)
     * @return Updated ProductResponse
     */
    ProductResponse updateProduct(UUID productId, ProductUpdateRequest request);

    /**
     * Deletes a product from the system.
     * @param productId UUID of the product
     */
    void deleteProduct(UUID productId);

    /**
     * Fetch all products for a specific business.
     * @param businessId UUID of the business
     * @return List of ProductResponse
     */
    List<ProductResponse> getAllProductsByBusiness(UUID businessId);

    /**
     * Fetch a single product by its ID.
     * @param productId UUID of the product
     * @return ProductResponse
     */
    ProductResponse getProductById(UUID productId);

    /**
     * Adjust product stock manually (e.g., due to loss, shipment, etc.)
     * @param productId UUID of the product
     * @param quantityChange Amount to change (positive for increase, negative for decrease)
     * @param reason Reason for the adjustment
     * @return Updated ProductResponse
     */
    ProductResponse adjustProductStock(UUID productId, int quantityChange, String reason);

    /**
     * Adjust product stock to specific values.
     * @param productId UUID of the product
     * @param newPackets New number of packets
     * @param newItemsPerPacket New number of items per packet
     * @return Updated ProductResponse
     */
    ProductResponse adjustStock(UUID productId, int newPackets, int newItemsPerPacket);

    /**
     * Search for products by keyword for a specific business
     * @param businessId UUID of the business
     * @param keyword Search string
     * @return List of matching ProductResponses
     */
    List<ProductResponse> searchProducts(UUID businessId, String keyword);

    /**
     * Get low-stock products based on a threshold.
     * @param businessId UUID of the business
     * @param threshold Total items count below which the product is considered low-stock
     * @return List of low-stock ProductResponses
     */
    List<ProductResponse> getLowStockProducts(UUID businessId, int threshold);
}
