package com.shopwise.Services.productimage;

import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Dto.productimage.ProductImageResponse;
import com.shopwise.Repository.ProductImageRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Services.cloudinary.CloudinaryService;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Product;
import com.shopwise.models.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final DailySummaryService dailySummaryService;
    private final CloudinaryService cloudinaryService;

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
        String publicId = image.getPublicId();
        
        // Delete the image from database
        productImageRepository.delete(image);
        
        // Delete the image from Cloudinary
        try {
            boolean deleted = cloudinaryService.deleteImage(publicId);
            if (!deleted) {
                log.warn("Failed to delete image from Cloudinary: {}", publicId);
            }
        } catch (Exception e) {
            // Log the error but don't fail the transaction
            log.error("Error deleting image from Cloudinary: {}", publicId, e);
        }
        
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
        
        // Get all images for the product
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        int imageCount = images.size();
        
        if (imageCount > 0) {
            // Delete images from Cloudinary
            for (ProductImage image : images) {
                try {
                    boolean deleted = cloudinaryService.deleteImage(image.getPublicId());
                    if (!deleted) {
                        log.warn("Failed to delete image from Cloudinary: {}", image.getPublicId());
                    }
                } catch (Exception e) {
                    // Log the error but don't fail the transaction
                    log.error("Error deleting image from Cloudinary: {}", image.getPublicId(), e);
                }
            }
            
            // Delete all images from database
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
