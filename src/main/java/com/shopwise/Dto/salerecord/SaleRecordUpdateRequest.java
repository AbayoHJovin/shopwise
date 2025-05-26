package com.shopwise.Dto.salerecord;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleRecordUpdateRequest {
    
    private UUID productId;
    

    @Min(value = 0, message = "Packets sold must be non-negative")
    private Integer packetsSold;
    
    @Min(value = 0, message = "Pieces sold must be non-negative")
    private Integer piecesSold;
    
    private LocalDateTime saleTime;
    
    private Boolean manuallyAdjusted;
    
    private Boolean loggedLater;
    
    private String notes;
    
    private LocalDateTime actualSaleTime;
    
    /**
     * Validates that at least one of packetsSold or piecesSold is provided and greater than zero
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValidSale() {
        return (packetsSold != null && packetsSold > 0) || 
               (piecesSold != null && piecesSold > 0);
    }
    
    /**
     * Calculates the total quantity sold in terms of individual pieces
     * 
     * @param itemsPerPacket The number of items in each packet for this product
     * @return The total number of individual pieces sold
     */
    public int calculateTotalPiecesSold(int itemsPerPacket) {
        int packetsTotal = (packetsSold != null) ? packetsSold : 0;
        int piecesTotal = (piecesSold != null) ? piecesSold : 0;
        
        return (packetsTotal * itemsPerPacket) + piecesTotal;
    }
}
