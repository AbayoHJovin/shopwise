package com.shopwise.Services.salerecord;

import com.shopwise.Dto.salerecord.SaleRecordRequest;
import com.shopwise.Dto.salerecord.SaleRecordResponse;
import com.shopwise.Dto.salerecord.SaleRecordUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.ProductRepository;
import com.shopwise.Repository.SaleRecordRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
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
    private final DailySummaryService dailySummaryService;

    @Override
    @Transactional
    public SaleRecordResponse logSale(UUID businessId, SaleRecordRequest request) {
        // Validate the sale request
        if (!request.isValidSale()) {
            throw SaleRecordException.badRequest("At least one of packetsSold or piecesSold must be provided and greater than zero");
        }
        
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
        
        // Calculate total pieces being sold
        int itemsPerPacket = product.getItemsPerPacket();
        int totalPiecesSold = request.calculateTotalPiecesSold(itemsPerPacket);
        
        // Check if there's enough stock
        int currentStockInPieces = product.getPackets() * itemsPerPacket;
        if (currentStockInPieces < totalPiecesSold) {
            throw SaleRecordException.badRequest("Not enough stock available. Current stock: " + 
                    currentStockInPieces + " pieces, Requested: " + totalPiecesSold + " pieces");
        }
        
        // Create new sale record
        SaleRecord saleRecord = new SaleRecord();
        saleRecord.setPacketsSold(request.getPacketsSold() != null ? request.getPacketsSold() : 0);
        saleRecord.setPiecesSold(request.getPiecesSold() != null ? request.getPiecesSold() : 0);
        saleRecord.setTotalPiecesSold(totalPiecesSold);
        
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
        
        // Update product stock - calculate remaining pieces and convert back to packets
        int remainingPieces = currentStockInPieces - totalPiecesSold;
        int newPackets = remainingPieces / itemsPerPacket;
        
        // If there are leftover pieces, we need to handle them appropriately
        // In a real-world scenario, we might want to track these separately
        // For now, we'll round up to the nearest packet
        if (remainingPieces % itemsPerPacket > 0) {
            newPackets++;
        }
        
        product.setPackets(newPackets);
        productRepository.save(product);
        
        // Log the sale in daily summary
        double totalValue = savedSaleRecord.getTotalPiecesSold() * product.getPricePerItem();
        String formattedAmount = String.format("%.2f", totalValue);
        
        // Create a detailed sale description
        StringBuilder saleDescription = new StringBuilder("Sale recorded: ");
        if (savedSaleRecord.getPacketsSold() > 0) {
            saleDescription.append(savedSaleRecord.getPacketsSold()).append(" packet(s)");
            if (savedSaleRecord.getPiecesSold() > 0) {
                saleDescription.append(" and ");
            }
        }
        if (savedSaleRecord.getPiecesSold() > 0) {
            saleDescription.append(savedSaleRecord.getPiecesSold()).append(" piece(s)");
        }
        saleDescription.append(" of '").append(product.getName()).append("' for a total of ").append(formattedAmount);
        
        dailySummaryService.logDailyAction(businessId, saleDescription.toString());
        
        // Return response
        return mapToSaleRecordResponse(savedSaleRecord);
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
        // Validate the sale update request if packets or pieces are provided
        if ((request.getPacketsSold() != null || request.getPiecesSold() != null) && !request.isValidSale()) {
            throw SaleRecordException.badRequest("At least one of packetsSold or piecesSold must be greater than zero");
        }
        
        // Find the sale record
        SaleRecord saleRecord = saleRecordRepository.findById(saleId)
                .orElseThrow(() -> SaleRecordException.notFound("Sale record not found with ID: " + saleId));
        
        // Get the original product and quantities for stock calculations
        Product product = saleRecord.getProduct();
        int originalPacketsSold = saleRecord.getPacketsSold();
        int originalPiecesSold = saleRecord.getPiecesSold();
        int originalTotalPiecesSold = saleRecord.getTotalPiecesSold();
        
        // Check if product is being changed
        if (request.getProductId() != null && !request.getProductId().equals(product.getId())) {
            // Find the new product
            Product newProduct = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> SaleRecordException.notFound("New product not found with ID: " + request.getProductId()));
            
            // Get the new quantities (or use original if not provided)
            int newPacketsSold = request.getPacketsSold() != null ? request.getPacketsSold() : originalPacketsSold;
            int newPiecesSold = request.getPiecesSold() != null ? request.getPiecesSold() : originalPiecesSold;
            int newTotalPiecesSold = (newPacketsSold * newProduct.getItemsPerPacket()) + newPiecesSold;
            
            // Validate that the new product belongs to the same business
            if (!newProduct.getBusiness().getId().equals(saleRecord.getBusiness().getId())) {
                throw SaleRecordException.badRequest("New product does not belong to the same business");
            }
            
            // Restore stock to original product
            int currentOriginalStockInPieces = product.getPackets() * product.getItemsPerPacket();
            int newOriginalStockInPieces = currentOriginalStockInPieces + originalTotalPiecesSold;
            int originalItemsPerPacket = product.getItemsPerPacket();
            int newOriginalPackets = newOriginalStockInPieces / originalItemsPerPacket;
            if (newOriginalStockInPieces % originalItemsPerPacket > 0) {
                newOriginalPackets++;
            }
            product.setPackets(newOriginalPackets);
            productRepository.save(product);
            
            // Check if there's enough stock in the new product
            int currentNewStockInPieces = newProduct.getPackets() * newProduct.getItemsPerPacket();
            if (currentNewStockInPieces < newTotalPiecesSold) {
                throw SaleRecordException.badRequest("Not enough stock available in the new product. Current stock: " + 
                        currentNewStockInPieces + " pieces, Requested: " + newTotalPiecesSold + " pieces");
            }
            
            // Update new product stock
            int remainingNewPieces = currentNewStockInPieces - newTotalPiecesSold;
            int newItemsPerPacket = newProduct.getItemsPerPacket();
            int newNewPackets = remainingNewPieces / newItemsPerPacket;
            if (remainingNewPieces % newItemsPerPacket > 0) {
                newNewPackets++;
            }
            newProduct.setPackets(newNewPackets);
            productRepository.save(newProduct);
            
            // Update sale record with new product
            saleRecord.setProduct(newProduct);
            saleRecord.setPacketsSold(newPacketsSold);
            saleRecord.setPiecesSold(newPiecesSold);
            saleRecord.setTotalPiecesSold(newTotalPiecesSold);
        } else if (request.getPacketsSold() != null || request.getPiecesSold() != null) {
            // Only quantities are being changed
            int newPacketsSold = request.getPacketsSold() != null ? request.getPacketsSold() : originalPacketsSold;
            int newPiecesSold = request.getPiecesSold() != null ? request.getPiecesSold() : originalPiecesSold;
            int newTotalPiecesSold = (newPacketsSold * product.getItemsPerPacket()) + newPiecesSold;
            
            // Calculate stock difference
            int currentStockInPieces = product.getPackets() * product.getItemsPerPacket();
            int stockDifferenceInPieces = originalTotalPiecesSold - newTotalPiecesSold;
            
            if (stockDifferenceInPieces < 0 && currentStockInPieces < Math.abs(stockDifferenceInPieces)) {
                throw SaleRecordException.badRequest("Not enough stock available for the increased quantity. Current stock: " + 
                        currentStockInPieces + " pieces, Additional needed: " + Math.abs(stockDifferenceInPieces) + " pieces");
            }
            
            // Update product stock
            int newTotalPieces = currentStockInPieces + stockDifferenceInPieces;
            int itemsPerPacket = product.getItemsPerPacket();
            int newPackets = newTotalPieces / itemsPerPacket;
            if (newTotalPieces % itemsPerPacket > 0) {
                newPackets++;
            }
            product.setPackets(newPackets);
            productRepository.save(product);
            
            // Update sale record quantities
            saleRecord.setPacketsSold(newPacketsSold);
            saleRecord.setPiecesSold(newPiecesSold);
            saleRecord.setTotalPiecesSold(newTotalPiecesSold);
        }
        
        // Update sale record fields
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
        
        // Log the sale update in daily summary
        Product updatedProduct = updatedSaleRecord.getProduct();
        double totalValue = updatedSaleRecord.getTotalPiecesSold() * updatedProduct.getPricePerItem();
        String formattedAmount = String.format("%.2f", totalValue);
        
        StringBuilder updateDetails = new StringBuilder();
        updateDetails.append("Sale updated for product '").append(updatedProduct.getName()).append("': ");
        
        if (request.getProductId() != null && !request.getProductId().equals(product.getId())) {
            updateDetails.append("product changed, ");
        }
        
        if (request.getPacketsSold() != null && request.getPacketsSold() != originalPacketsSold) {
            updateDetails.append("packets changed from ").append(originalPacketsSold)
                    .append(" to ").append(request.getPacketsSold()).append(", ");
        }
        
        if (request.getPiecesSold() != null && request.getPiecesSold() != originalPiecesSold) {
            updateDetails.append("pieces changed from ").append(originalPiecesSold)
                    .append(" to ").append(request.getPiecesSold()).append(", ");
        }
        
        if (request.getSaleTime() != null) {
            updateDetails.append("sale time updated, ");
        }
        
        // Remove trailing comma and space if present
        String logMessage = updateDetails.toString();
        if (logMessage.endsWith(", ")) {
            logMessage = logMessage.substring(0, logMessage.length() - 2);
        }
        
        logMessage += ". New total value: " + formattedAmount;
        
        dailySummaryService.logDailyAction(updatedSaleRecord.getBusiness().getId(), logMessage);
        
        // Return response
        return mapToSaleRecordResponse(updatedSaleRecord);
    }
    
    // Helper method to map SaleRecord entity to SaleRecordResponse DTO
    private SaleRecordResponse mapToSaleRecordResponse(SaleRecord saleRecord) {
        Product product = saleRecord.getProduct();
        double totalSaleValue = saleRecord.getTotalPiecesSold() * product.getPricePerItem();
        
        return SaleRecordResponse.builder()
                .id(saleRecord.getId())
                .packetsSold(saleRecord.getPacketsSold())
                .piecesSold(saleRecord.getPiecesSold())
                .totalPiecesSold(saleRecord.getTotalPiecesSold())
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
