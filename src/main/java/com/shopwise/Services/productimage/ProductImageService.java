package com.shopwise.Services.productimage;

import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Dto.productimage.ProductImageResponse;

import java.util.List;
import java.util.UUID;

public interface ProductImageService {
    
    /**
     * Add an image to a product
     * 
     * @param productId Product ID
     * @param request Image details
     * @return Added image details
     */
    ProductImageResponse addProductImage(UUID productId, ProductImageRequest request);
    
    /**
     * Get all images for a product
     * 
     * @param productId Product ID
     * @return List of product images
     */
    List<ProductImageResponse> getProductImages(UUID productId);
    
    /**
     * Delete a product image
     * 
     * @param imageId Image ID
     */
    void deleteProductImage(UUID imageId);
    
    /**
     * Delete all images for a product
     * 
     * @param productId Product ID
     */
    void deleteAllProductImages(UUID productId);
}
