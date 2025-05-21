package com.shopwise.controllers;

import com.shopwise.Dto.product.ProductRequest;
import com.shopwise.Dto.product.ProductResponse;
import com.shopwise.Dto.product.ProductUpdateRequest;
import com.shopwise.Services.product.ProductException;
import com.shopwise.Services.product.ProductService;
import com.shopwise.models.User;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/business/{businessId}")
    public ResponseEntity<?> addProduct(@PathVariable UUID businessId,
                                       @Valid @RequestBody ProductRequest request,
                                       Authentication authentication) {
        try {
            // Authentication is handled by Spring Security using JWT from HTTP-only cookies
            // The user is automatically extracted from the token
            
            ProductResponse createdProduct = productService.addProduct(businessId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
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

    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> getAllProductsByBusiness(@PathVariable UUID businessId,
                                                    Authentication authentication) {
        try {
            List<ProductResponse> products = productService.getAllProductsByBusiness(businessId);
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

    @GetMapping("/business/{businessId}/search")
    public ResponseEntity<?> searchProducts(@PathVariable UUID businessId,
                                           @RequestParam String keyword,
                                           Authentication authentication) {
        try {
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
