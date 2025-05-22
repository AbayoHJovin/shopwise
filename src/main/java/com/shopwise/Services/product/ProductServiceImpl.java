 package com.shopwise.Services.product;

import com.shopwise.Dto.product.ProductRequest;
import com.shopwise.Dto.product.ProductResponse;
import com.shopwise.Dto.product.ProductUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import com.shopwise.models.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public ProductResponse addProduct(UUID businessId, ProductRequest request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> ProductException.notFound("Business not found with ID: " + businessId));
        
        // Create new product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPackets(request.getPackets());
        product.setItemsPerPacket(request.getItemsPerPacket());
        product.setPricePerItem(request.getPricePerItem());
        product.setFulfillmentCost(request.getFulfillmentCost());
        product.setBusiness(business);
        
        // Save product
        Product savedProduct = productRepository.save(product);
        
        // Log the product addition in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Product '" + savedProduct.getName() + "' was added with " + 
                savedProduct.getPackets() + " packets of " + savedProduct.getItemsPerPacket() + " items each");
        
        // Return response
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID productId, ProductUpdateRequest request) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductException.notFound("Product not found with ID: " + productId));
        
        // Update fields if provided
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        
        if (request.getPackets() != null) {
            product.setPackets(request.getPackets());
        }
        
        if (request.getItemsPerPacket() != null) {
            product.setItemsPerPacket(request.getItemsPerPacket());
        }
        
        if (request.getPricePerItem() != null) {
            product.setPricePerItem(request.getPricePerItem());
        }
        
        if (request.getFulfillmentCost() != null) {
            product.setFulfillmentCost(request.getFulfillmentCost());
        }
        
        // Save updated product
        Product updatedProduct = productRepository.save(product);
        
        // Log the product update in daily summary
        StringBuilder updateDetails = new StringBuilder();
        updateDetails.append("Product '").append(updatedProduct.getName()).append("' was updated: ");
        
        if (request.getName() != null) {
            updateDetails.append("name updated, ");
        }
        
        if (request.getDescription() != null) {
            updateDetails.append("description updated, ");
        }
        
        if (request.getPackets() != null) {
            updateDetails.append("packets updated to ").append(request.getPackets()).append(", ");
        }
        
        if (request.getItemsPerPacket() != null) {
            updateDetails.append("items per packet updated to ").append(request.getItemsPerPacket()).append(", ");
        }
        
        if (request.getPricePerItem() != null) {
            updateDetails.append("price per item updated to ").append(String.format("%.2f", request.getPricePerItem())).append(", ");
        }
        
        if (request.getFulfillmentCost() != null) {
            updateDetails.append("fulfillment cost updated to ").append(String.format("%.2f", request.getFulfillmentCost())).append(", ");
        }
        
        // Remove trailing comma and space if present
        String logMessage = updateDetails.toString();
        if (logMessage.endsWith(", ")) {
            logMessage = logMessage.substring(0, logMessage.length() - 2);
        }
        
        dailySummaryService.logDailyAction(updatedProduct.getBusiness().getId(), logMessage);
        
        // Return response
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        // Find the product to get its details before deletion
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductException.notFound("Product not found with ID: " + productId));
        
        String productName = product.getName();
        UUID businessId = product.getBusiness().getId();
        
        // Delete product
        productRepository.deleteById(productId);
        
        // Log the product deletion in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Product '" + productName + "' was deleted");
    }

    @Override
    public List<ProductResponse> getAllProductsByBusiness(UUID businessId) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw ProductException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all products for the business
        List<Product> products = productRepository.findByBusinessId(businessId);
        
        // Map to response DTOs
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(UUID productId) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductException.notFound("Product not found with ID: " + productId));
        
        // Return response
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse adjustProductStock(UUID productId, int quantityChange, String reason) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductException.notFound("Product not found with ID: " + productId));
        
        // Calculate new stock level
        int currentPackets = product.getPackets();
        int newPackets = currentPackets + quantityChange;
        
        // Validate new stock level
        if (newPackets < 0) {
            throw ProductException.badRequest("Cannot reduce stock below zero. Current stock: " + 
                    currentPackets + " packets, Requested change: " + quantityChange);
        }
        
        // Update stock
        product.setPackets(newPackets);
        
        // Save updated product
        Product updatedProduct = productRepository.save(product);
        
        // Log the stock adjustment in daily summary
        String changeDescription = quantityChange > 0 ? "increased by " + quantityChange : "decreased by " + Math.abs(quantityChange);
        dailySummaryService.logDailyAction(updatedProduct.getBusiness().getId(), 
                "Stock for product '" + updatedProduct.getName() + "' was " + changeDescription + 
                " packets" + (reason != null && !reason.isEmpty() ? ". Reason: " + reason : ""));
        
        // Return response
        return mapToProductResponse(updatedProduct);
    }
    
    @Override
    @Transactional
    public ProductResponse adjustStock(UUID productId, int newPackets, int newItemsPerPacket) {
        // Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductException.notFound("Product not found with ID: " + productId));
        
        // Validate new stock values
        if (newPackets < 0) {
            throw ProductException.badRequest("Number of packets cannot be negative");
        }
        
        if (newItemsPerPacket <= 0) {
            throw ProductException.badRequest("Items per packet must be greater than zero");
        }
        
        // Get original values for logging
        int originalPackets = product.getPackets();
        int originalItemsPerPacket = product.getItemsPerPacket();
        
        // Update stock values
        product.setPackets(newPackets);
        product.setItemsPerPacket(newItemsPerPacket);
        
        // Save updated product
        Product updatedProduct = productRepository.save(product);
        
        // Log the stock adjustment in daily summary
        dailySummaryService.logDailyAction(updatedProduct.getBusiness().getId(), 
                "Stock for product '" + updatedProduct.getName() + "' was adjusted from " + 
                originalPackets + " packets of " + originalItemsPerPacket + " items to " + 
                newPackets + " packets of " + newItemsPerPacket + " items");
        
        // Return response
        return mapToProductResponse(updatedProduct);
    }
    
    @Override
    public List<ProductResponse> searchProducts(UUID businessId, String keyword) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw ProductException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all products for the business
        List<Product> products = productRepository.findByBusinessId(businessId);
        
        // Filter products by keyword (case-insensitive)
        String lowercaseKeyword = keyword.toLowerCase();
        
        return products.stream()
                .filter(product -> 
                    product.getName().toLowerCase().contains(lowercaseKeyword) ||
                    (product.getDescription() != null && 
                     product.getDescription().toLowerCase().contains(lowercaseKeyword))
                )
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductResponse> getLowStockProducts(UUID businessId, int threshold) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw ProductException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all products for the business
        List<Product> products = productRepository.findByBusinessId(businessId);
        
        // Filter products by total items below threshold
        return products.stream()
                .filter(product -> (product.getPackets() * product.getItemsPerPacket()) < threshold)
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
    
    // Helper method to map Product entity to ProductResponse DTO
    private ProductResponse mapToProductResponse(Product product) {
        int totalItems = product.getPackets() * product.getItemsPerPacket();
        double totalValue = totalItems * product.getPricePerItem();
        
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .packets(product.getPackets())
                .itemsPerPacket(product.getItemsPerPacket())
                .pricePerItem(product.getPricePerItem())
                .fulfillmentCost(product.getFulfillmentCost())
                .businessId(product.getBusiness().getId())
                .totalItems(totalItems)
                .totalValue(totalValue)
                .build();
    }
}
