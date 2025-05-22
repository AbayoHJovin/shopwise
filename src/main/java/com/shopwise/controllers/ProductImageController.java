package com.shopwise.controllers;

import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Dto.productimage.ProductImageResponse;
import com.shopwise.Services.productimage.ProductImageException;
import com.shopwise.Services.productimage.ProductImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Add an image to a product
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
