package com.shopwise.Services.productimage;

import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Dto.productimage.ProductImageResponse;
import com.shopwise.Repository.ProductImageRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Product;
import com.shopwise.models.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public ProductImageResponse addProductImage(UUID productId, ProductImageRequest request) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductImageException.notFound("Product not found with ID: " + productId));
        
        // Check if the product already has 3 images
        if (product.getImages().size() >= 3) {
            throw ProductImageException.badRequest("Product already has the maximum of 3 images");
        }
        
        // Create and save the product image
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(request.getImageUrl());
        productImage.setPublicId(request.getPublicId());
        productImage.setProduct(product);
        
        ProductImage savedImage = productImageRepository.save(productImage);
        
        // Log the action in the daily summary
        dailySummaryService.logDailyAction(product.getBusiness().getId(), 
                "Image added to product '" + product.getName() + "'");
        
        return mapToResponse(savedImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(UUID productId) {
        // Check if the product exists
        if (!productRepository.existsById(productId)) {
            throw ProductImageException.notFound("Product not found with ID: " + productId);
        }
        
        // Get all images for the product
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        
        return images.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteProductImage(UUID imageId) {
        // Find the image
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> ProductImageException.notFound("Product image not found with ID: " + imageId));
        
        Product product = image.getProduct();
        
        // Delete the image
        productImageRepository.delete(image);
        
        // Log the action in the daily summary
        dailySummaryService.logDailyAction(product.getBusiness().getId(), 
                "Image removed from product '" + product.getName() + "'");
    }

    @Override
    @Transactional
    public void deleteAllProductImages(UUID productId) {
        // Check if the product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductImageException.notFound("Product not found with ID: " + productId));
        
        // Get the number of images for logging
        int imageCount = product.getImages().size();
        
        if (imageCount > 0) {
            // Delete all images for the product
            productImageRepository.deleteByProductId(productId);
            
            // Log the action in the daily summary
            dailySummaryService.logDailyAction(product.getBusiness().getId(), 
                    imageCount + " image(s) removed from product '" + product.getName() + "'");
        }
    }
    
    /**
     * Maps a ProductImage entity to a ProductImageResponse DTO
     * 
     * @param image The ProductImage entity
     * @return The ProductImageResponse DTO
     */
    private ProductImageResponse mapToResponse(ProductImage image) {
        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .publicId(image.getPublicId())
                .productId(image.getProduct().getId())
                .build();
    }
}
