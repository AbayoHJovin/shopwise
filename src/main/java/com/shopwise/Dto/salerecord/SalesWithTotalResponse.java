package com.shopwise.Dto.salerecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO containing a list of sales and the total amount
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesWithTotalResponse {
    private List<SaleRecordResponse> sales;
    private double totalAmount;
}
