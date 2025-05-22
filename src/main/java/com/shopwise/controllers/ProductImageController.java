package com.shopwise.controllers;

import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Dto.productimage.ProductImageResponse;
import com.shopwise.Services.cloudinary.CloudinaryException;
import com.shopwise.Services.cloudinary.CloudinaryService;
import com.shopwise.Services.productimage.ProductImageException;
import com.shopwise.Services.productimage.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ProductImageService productImageService;
    private final CloudinaryService cloudinaryService;
    
    // Thread pool for parallel image uploads
    private final ExecutorService executorService = Executors.newFixedThreadPool(3); // 3 threads for max 3 images

    /**
     * Upload an image to Cloudinary and add it to a product
     * 
     * @param productId Product ID
     * @param file Image file to upload
     * @param authentication Authentication object with user details
     * @return Added image details
     */
    @PostMapping(value = "/upload/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImage(
            @PathVariable UUID productId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            // Upload image to Cloudinary (in 'products' folder)
            Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "products");
            
            // Create product image request with Cloudinary details
            ProductImageRequest request = new ProductImageRequest();
            request.setImageUrl(uploadResult.get("imageUrl"));
            request.setPublicId(uploadResult.get("publicId"));
            
            // Add image to product
            ProductImageResponse response = productImageService.addProductImage(productId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (CloudinaryException | ProductImageException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e instanceof CloudinaryException ? 
                    ((CloudinaryException) e).getStatus() : 
                    ((ProductImageException) e).getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Add an image to a product (direct URL method)
     * 
     * @param productId Product ID
     * @param request Image details
     * @param authentication Authentication object with user details
     * @return Added image details
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> addProductImage(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductImageRequest request,
            Authentication authentication) {
        try {
            ProductImageResponse response = productImageService.addProductImage(productId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ProductImageException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload multiple images to Cloudinary and add them to a product concurrently
     * 
     * @param productId Product ID
     * @param files Image files to upload (max 3)
     * @param authentication Authentication object with user details
     * @return List of added image details
     */
    @PostMapping(value = "/upload/product/{productId}/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMultipleProductImages(
            @PathVariable UUID productId,
            @RequestParam("files") MultipartFile[] files,
            Authentication authentication) {
        try {
            // Validate number of files
            if (files.length > 3) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Maximum 3 images allowed per product");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Get current image count for the product
            List<ProductImageResponse> existingImages = productImageService.getProductImages(productId);
            int availableSlots = 3 - existingImages.size();
            
            if (availableSlots <= 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Product already has the maximum of 3 images");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            // Limit files to available slots
            MultipartFile[] filesToUpload = files;
            if (files.length > availableSlots) {
                filesToUpload = Arrays.copyOf(files, availableSlots);
                log.warn("Only uploading {} out of {} images due to the 3 image limit", availableSlots, files.length);
            }
            
            // Create a list to hold the futures
            List<CompletableFuture<ProductImageResponse>> futures = new ArrayList<>();
            
            // Process each file concurrently
            for (MultipartFile file : filesToUpload) {
                CompletableFuture<ProductImageResponse> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        // Upload image to Cloudinary
                        Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "products");
                        
                        // Create product image request
                        ProductImageRequest request = new ProductImageRequest();
                        request.setImageUrl(uploadResult.get("imageUrl"));
                        request.setPublicId(uploadResult.get("publicId"));
                        
                        // Add image to product
                        return productImageService.addProductImage(productId, request);
                    } catch (Exception e) {
                        log.error("Error processing image upload", e);
                        throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
                    }
                }, executorService);
                
                futures.add(future);
            }
            
            // Wait for all futures to complete and collect results
            List<ProductImageResponse> responses = new ArrayList<>();
            for (CompletableFuture<ProductImageResponse> future : futures) {
                try {
                    responses.add(future.join());
                } catch (Exception e) {
                    log.error("Error getting result from future", e);
                    // Continue with other images even if one fails
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (ProductImageException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            log.error("Error uploading multiple images", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get all images for a product
     * 
     * @param productId Product ID
     * @param authentication Authentication object with user details
     * @return List of product images
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductImages(
            @PathVariable UUID productId,
            Authentication authentication) {
        try {
            List<ProductImageResponse> images = productImageService.getProductImages(productId);
            return ResponseEntity.ok(images);
        } catch (ProductImageException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete a product image
     * 
     * @param imageId Image ID
     * @param authentication Authentication object with user details
     * @return Success message
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable UUID imageId,
            Authentication authentication) {
        try {
            productImageService.deleteProductImage(imageId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product image deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ProductImageException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
