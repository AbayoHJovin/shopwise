package com.shopwise.controllers;

import com.shopwise.Dto.product.ProductRequest;
import com.shopwise.Dto.product.ProductResponse;
import com.shopwise.Dto.product.ProductUpdateRequest;
import com.shopwise.Dto.productimage.ProductImageRequest;
import com.shopwise.Services.cloudinary.CloudinaryService;
import com.shopwise.Services.product.ProductException;
import com.shopwise.Services.product.ProductService;
import com.shopwise.models.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    /**
     * Add a new product to the selected business
     * 
     * @param request The product details
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object containing user details
     * @return The created product
     */
    @PostMapping
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request,
                                       HttpServletRequest httpRequest,
                                       Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the selected business ID from cookies
            Cookie[] cookies = httpRequest.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                // Convert string to UUID
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Verify that the user is the owner or a collaborator of the business
                boolean isOwnerOrCollaborator = productService.isUserOwnerOrCollaborator(businessId, currentUser.getId());
                if (!isOwnerOrCollaborator) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You don't have permission to add products to this business"));
                }
                
                // Add the product
                ProductResponse createdProduct = productService.addProduct(businessId, request);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable UUID productId,
                                          @Valid @RequestBody ProductUpdateRequest request,
                                          Authentication authentication) {
        try {
            ProductResponse updatedProduct = productService.updateProduct(productId, request);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId,
                                          Authentication authentication) {
        try {
            productService.deleteProduct(productId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ProductException e) {
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
     * Add a new product with optional image uploads
     * 
     * @param productRequest JSON data for the product
     * @param files Optional image files to upload (max 3)
     * @param httpRequest HTTP request to extract the business cookie
     * @param authentication The authentication object
     * @return The created product with image details
     */
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProductWithImages(
            @RequestPart("product") @Valid ProductRequest productRequest,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = (User) authentication.getPrincipal();
            
            // Get the selected business ID from cookies
            Cookie[] cookies = httpRequest.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                // Convert string to UUID
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Verify that the user is the owner or a collaborator of the business
                boolean isOwnerOrCollaborator = productService.isUserOwnerOrCollaborator(businessId, currentUser.getId());
                if (!isOwnerOrCollaborator) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "You don't have permission to add products to this business"));
                }
                
                // Process image uploads if provided
                List<ProductImageRequest> imageRequests = new ArrayList<>();
                if (files != null && files.length > 0) {
                    // Limit to max 3 images
                    int imagesToProcess = Math.min(files.length, 3);
                    
                    for (int i = 0; i < imagesToProcess; i++) {
                        MultipartFile file = files[i];
                        if (!file.isEmpty()) {
                            try {
                                // Upload image to Cloudinary
                                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "products");
                                
                                // Create image request
                                ProductImageRequest imageRequest = new ProductImageRequest();
                                imageRequest.setImageUrl(uploadResult.get("imageUrl"));
                                imageRequest.setPublicId(uploadResult.get("publicId"));
                                imageRequests.add(imageRequest);
                            } catch (Exception e) {
                                // Log error but continue with other images
                                // This ensures product creation doesn't fail if one image upload fails
                                log.error("Error uploading image for product: {}", e.getMessage());
                            }
                        }
                    }
                }
                
                // Set images in the product request
                productRequest.setImages(imageRequests);
                
                // Add the product
                ProductResponse createdProduct = productService.addProduct(businessId, productRequest);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
        } catch (ProductException e) {
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
     * Get all products for the selected business with pagination support
     * 
     * @param page The page number (0-based)
     * @param size The page size
     * @param request HTTP request to extract the business cookie
     * @param authentication The authentication object
     * @return Paginated list of products with metadata
     */
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request,
            Authentication authentication) {
        try {
            // Extract user from authentication
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "User not authenticated"));
            }
            
            // Get the selected business ID from cookies
            Cookie[] cookies = request.getCookies();
            String selectedBusinessIdStr = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        selectedBusinessIdStr = cookie.getValue();
                        break;
                    }
                }
            }
            
            // If no business is selected, return appropriate response
            if (selectedBusinessIdStr == null || selectedBusinessIdStr.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No business selected. Please select a business first."));
            }
            
            try {
                UUID businessId = UUID.fromString(selectedBusinessIdStr);
                
                // Get paginated products
                Map<String, Object> paginatedProducts = productService.getAllProductsByBusinessPaginated(businessId, page, size);
                
                // Check if there are more products to load
                boolean hasMore = (boolean) paginatedProducts.getOrDefault("hasMore", false);
                
                // Handle the unchecked cast with proper type safety
                @SuppressWarnings("unchecked") // This suppresses the warning as we know the actual type
                List<ProductResponse> products = (List<ProductResponse>) paginatedProducts.get("products");
                
                long totalProducts = (long) paginatedProducts.get("totalCount");
                
                Map<String, Object> response = new HashMap<>();
                response.put("products", products);
                response.put("currentPage", page);
                response.put("pageSize", size);
                response.put("totalCount", totalProducts);
                response.put("hasMore", hasMore);
                
                // Add a message when all products have been loaded
                if (!hasMore && page > 0) {
                    response.put("message", "All products have been loaded");
                }
                
                return ResponseEntity.ok(response);
                
            } catch (IllegalArgumentException e) {
                // Invalid UUID format
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid business ID format: " + selectedBusinessIdStr));
            }
            
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable UUID productId,
                                           Authentication authentication) {
        try {
            ProductResponse product = productService.getProductById(productId);
            return ResponseEntity.ok(product);
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{productId}/adjust-stock")
    public ResponseEntity<?> adjustProductStock(@PathVariable UUID productId,
                                              @RequestParam int quantityChange,
                                              @RequestParam String reason,
                                              Authentication authentication) {
        try {
            ProductResponse updatedProduct = productService.adjustProductStock(productId, quantityChange, reason);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/{productId}/set-stock")
    public ResponseEntity<?> adjustStock(@PathVariable UUID productId,
                                        @RequestParam int newPackets,
                                        @RequestParam int newItemsPerPacket,
                                        Authentication authentication) {
        try {
            ProductResponse updatedProduct = productService.adjustStock(productId, newPackets, newItemsPerPacket);
            return ResponseEntity.ok(updatedProduct);
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword,
                                           HttpServletRequest httpRequest) {
        try {
            String businessIdStr = null;
            Cookie[] cookies = httpRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("selectedBusiness".equals(cookie.getName())) {
                        businessIdStr = cookie.getValue();
                        break;
                    }
                }
            }

            // Check if business ID is available
            if (businessIdStr == null || businessIdStr.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No business selected. Please select a business first.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            UUID businessId = UUID.fromString(businessIdStr);

            List<ProductResponse> products = productService.searchProducts(businessId, keyword);
            return ResponseEntity.ok(products);
        } catch (ProductException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/business/{businessId}/low-stock")
    public ResponseEntity<?> getLowStockProducts(@PathVariable UUID businessId,
                                               @RequestParam(defaultValue = "10") int threshold,
                                               Authentication authentication) {
        try {
            List<ProductResponse> products = productService.getLowStockProducts(businessId, threshold);
            return ResponseEntity.ok(products);
        } catch (ProductException e) {
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
