package com.shopwise.Services.salerecord;

import com.shopwise.Dto.salerecord.SaleRecordRequest;
import com.shopwise.Dto.salerecord.SaleRecordResponse;
import com.shopwise.Dto.salerecord.SaleRecordUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Repository.SaleRecordRepository;
import com.shopwise.models.Business;
import com.shopwise.models.Product;
import com.shopwise.models.SaleRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleRecordServiceImpl implements SaleRecordService {

    private final SaleRecordRepository saleRecordRepository;
    private final BusinessRepository businessRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public SaleRecordResponse logSale(UUID businessId, SaleRecordRequest request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> SaleRecordException.notFound("Business not found with ID: " + businessId));
        
        // Find the product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> SaleRecordException.notFound("Product not found with ID: " + request.getProductId()));
        
        // Validate that the product belongs to the business
        if (!product.getBusiness().getId().equals(businessId)) {
            throw SaleRecordException.badRequest("Product does not belong to the specified business");
        }
        
        // Check if there's enough stock
        int currentStock = product.getPackets() * product.getItemsPerPacket();
        if (currentStock < request.getQuantitySold()) {
            throw SaleRecordException.badRequest("Not enough stock available. Current stock: " + 
                    currentStock + ", Requested: " + request.getQuantitySold());
        }
        
        // Create new sale record
        SaleRecord saleRecord = new SaleRecord();
        saleRecord.setQuantitySold(request.getQuantitySold());
        
        // Set sale time (use provided time or current time)
        LocalDateTime saleTime = request.getSaleTime() != null ? 
                request.getSaleTime() : LocalDateTime.now();
        saleRecord.setSaleTime(saleTime);
        
        saleRecord.setManuallyAdjusted(request.isManuallyAdjusted());
        saleRecord.setLoggedLater(request.isLoggedLater());
        saleRecord.setNotes(request.getNotes());
        
        // Set actual sale time if logged later
        if (request.isLoggedLater() && request.getActualSaleTime() != null) {
            saleRecord.setActualSaleTime(request.getActualSaleTime());
        } else {
            saleRecord.setActualSaleTime(saleTime);
        }
        
        saleRecord.setProduct(product);
        saleRecord.setBusiness(business);
        
        // Save sale record
        SaleRecord savedSaleRecord = saleRecordRepository.save(saleRecord);
        
        // Update product stock
        int remainingItems = currentStock - request.getQuantitySold();
        int itemsPerPacket = product.getItemsPerPacket();
        int newPackets = remainingItems / itemsPerPacket;
        int leftoverItems = remainingItems % itemsPerPacket;
        
        if (leftoverItems > 0) {
            // If there are leftover items, add an extra packet
            newPackets++;
            // In a real application, we might want to track the partial packet separately
        }
        
        product.setPackets(newPackets);
        productRepository.save(product);
        
        // Return response
        return mapToSaleRecordResponse(savedSaleRecord);
    }

    @Override
    public List<SaleRecordResponse> getSalesByProduct(UUID productId) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw SaleRecordException.notFound("Product not found with ID: " + productId);
        }
        
        // Get all sales for the product
        List<SaleRecord> saleRecords = saleRecordRepository.findByProductId(productId);
        
        // Map to response DTOs
        return saleRecords.stream()
                .map(this::mapToSaleRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleRecordResponse> getSalesByBusiness(UUID businessId) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw SaleRecordException.notFound("Business not found with ID: " + businessId);
        }
        
        // Get all sales for the business
        List<SaleRecord> saleRecords = saleRecordRepository.findByBusinessId(businessId);
        
        // Map to response DTOs
        return saleRecords.stream()
                .map(this::mapToSaleRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SaleRecordResponse> getSalesByDate(UUID businessId, LocalDate date) {
        // Check if business exists
        if (!businessRepository.existsById(businessId)) {
            throw SaleRecordException.notFound("Business not found with ID: " + businessId);
        }
        
        // Convert LocalDate to LocalDateTime range (start of day to end of day)
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // Get sales within date range
        List<SaleRecord> saleRecords = saleRecordRepository.findByBusinessIdAndDateRange(
                businessId, startOfDay, endOfDay);
        
        // Map to response DTOs
        return saleRecords.stream()
                .map(this::mapToSaleRecordResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SaleRecordResponse updateSale(UUID saleId, SaleRecordUpdateRequest request) {
        // Find the sale record
        SaleRecord saleRecord = saleRecordRepository.findById(saleId)
                .orElseThrow(() -> SaleRecordException.notFound("Sale record not found with ID: " + saleId));
        
        // Handle product change if requested
        Product product = saleRecord.getProduct();
        int originalQuantitySold = saleRecord.getQuantitySold();
        int newQuantitySold = request.getQuantitySold() != null ? request.getQuantitySold() : originalQuantitySold;
        
        if (request.getProductId() != null && !request.getProductId().equals(product.getId())) {
            // Product is being changed
            Product newProduct = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> SaleRecordException.notFound("Product not found with ID: " + request.getProductId()));
            
            // Validate that the new product belongs to the same business
            if (!newProduct.getBusiness().getId().equals(saleRecord.getBusiness().getId())) {
                throw SaleRecordException.badRequest("New product does not belong to the same business");
            }
            
            // Restore stock to original product
            int currentOriginalStock = product.getPackets() * product.getItemsPerPacket();
            int newOriginalStock = currentOriginalStock + originalQuantitySold;
            int originalItemsPerPacket = product.getItemsPerPacket();
            int newOriginalPackets = newOriginalStock / originalItemsPerPacket;
            if (newOriginalStock % originalItemsPerPacket > 0) {
                newOriginalPackets++;
            }
            product.setPackets(newOriginalPackets);
            productRepository.save(product);
            
            // Check if there's enough stock in the new product
            int currentNewStock = newProduct.getPackets() * newProduct.getItemsPerPacket();
            if (currentNewStock < newQuantitySold) {
                throw SaleRecordException.badRequest("Not enough stock available in the new product. Current stock: " + 
                        currentNewStock + ", Requested: " + newQuantitySold);
            }
            
            // Update new product stock
            int remainingNewItems = currentNewStock - newQuantitySold;
            int newItemsPerPacket = newProduct.getItemsPerPacket();
            int newNewPackets = remainingNewItems / newItemsPerPacket;
            if (remainingNewItems % newItemsPerPacket > 0) {
                newNewPackets++;
            }
            newProduct.setPackets(newNewPackets);
            productRepository.save(newProduct);
            
            // Update sale record with new product
            saleRecord.setProduct(newProduct);
        } else if (newQuantitySold != originalQuantitySold) {
            // Only quantity is being changed
            int currentStock = product.getPackets() * product.getItemsPerPacket();
            int stockDifference = originalQuantitySold - newQuantitySold;
            
            if (stockDifference < 0 && currentStock < Math.abs(stockDifference)) {
                throw SaleRecordException.badRequest("Not enough stock available for the increased quantity. Current stock: " + 
                        currentStock + ", Additional needed: " + Math.abs(stockDifference));
            }
            
            // Update product stock
            int newTotalItems = currentStock + stockDifference;
            int itemsPerPacket = product.getItemsPerPacket();
            int newPackets = newTotalItems / itemsPerPacket;
            if (newTotalItems % itemsPerPacket > 0) {
                newPackets++;
            }
            product.setPackets(newPackets);
            productRepository.save(product);
        }
        
        // Update sale record fields
        if (request.getQuantitySold() != null) {
            saleRecord.setQuantitySold(request.getQuantitySold());
        }
        
        if (request.getSaleTime() != null) {
            saleRecord.setSaleTime(request.getSaleTime());
        }
        
        if (request.getManuallyAdjusted() != null) {
            saleRecord.setManuallyAdjusted(request.getManuallyAdjusted());
        }
        
        if (request.getLoggedLater() != null) {
            saleRecord.setLoggedLater(request.getLoggedLater());
        }
        
        if (request.getNotes() != null) {
            saleRecord.setNotes(request.getNotes());
        }
        
        if (request.getActualSaleTime() != null) {
            saleRecord.setActualSaleTime(request.getActualSaleTime());
        }
        
        // Save updated sale record
        SaleRecord updatedSaleRecord = saleRecordRepository.save(saleRecord);
        
        // Return response
        return mapToSaleRecordResponse(updatedSaleRecord);
    }
    
    // Helper method to map SaleRecord entity to SaleRecordResponse DTO
    private SaleRecordResponse mapToSaleRecordResponse(SaleRecord saleRecord) {
        Product product = saleRecord.getProduct();
        double totalSaleValue = saleRecord.getQuantitySold() * product.getPricePerItem();
        
        return SaleRecordResponse.builder()
                .id(saleRecord.getId())
                .quantitySold(saleRecord.getQuantitySold())
                .saleTime(saleRecord.getSaleTime())
                .manuallyAdjusted(saleRecord.isManuallyAdjusted())
                .loggedLater(saleRecord.isLoggedLater())
                .notes(saleRecord.getNotes())
                .actualSaleTime(saleRecord.getActualSaleTime())
                .productId(product.getId())
                .productName(product.getName())
                .pricePerItem(product.getPricePerItem())
                .totalSaleValue(totalSaleValue)
                .businessId(saleRecord.getBusiness().getId())
                .businessName(saleRecord.getBusiness().getName())
                .build();
    }
}
