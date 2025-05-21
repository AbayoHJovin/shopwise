package com.shopwise.Services.salerecord;

import com.shopwise.Dto.salerecord.SaleRecordRequest;
import com.shopwise.Dto.salerecord.SaleRecordResponse;
import com.shopwise.Dto.salerecord.SaleRecordUpdateRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing sale records in the ShopWise system.
 */
public interface SaleRecordService {

    /**
     * Logs a new sale for a business.
     * @param businessId UUID of the business
     * @param request Sale details
     * @return Created SaleRecordResponse
     */
    SaleRecordResponse logSale(UUID businessId, SaleRecordRequest request);

    /**
     * Retrieves all sales for a specific product.
     * @param productId UUID of the product
     * @return List of SaleRecordResponse objects
     */
    List<SaleRecordResponse> getSalesByProduct(UUID productId);

    /**
     * Retrieves all sales for a specific business.
     * @param businessId UUID of the business
     * @return List of SaleRecordResponse objects
     */
    List<SaleRecordResponse> getSalesByBusiness(UUID businessId);

    /**
     * Retrieves sales for a business on a specific date.
     * @param businessId UUID of the business
     * @param date Date to filter sales
     * @return List of SaleRecordResponse objects
     */
    List<SaleRecordResponse> getSalesByDate(UUID businessId, LocalDate date);

    /**
     * Updates an existing sale record.
     * @param saleId UUID of the sale record to update
     * @param request Updated sale details
     * @return Updated SaleRecordResponse
     */
    SaleRecordResponse updateSale(UUID saleId, SaleRecordUpdateRequest request);
}
