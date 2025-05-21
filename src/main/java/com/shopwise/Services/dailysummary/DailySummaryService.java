package com.shopwise.Services.dailysummary;

import com.shopwise.Dto.dailysummary.DailySummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing daily summaries in the ShopWise system.
 */
public interface DailySummaryService {

    /**
     * Retrieves all daily summaries for a specific business.
     * @param businessId UUID of the business
     * @return List of DailySummaryResponse objects
     */
    List<DailySummaryResponse> getDailySummaries(UUID businessId);

    /**
     * Retrieves daily summaries for a business on a specific date.
     * @param businessId UUID of the business
     * @param date Date to filter summaries
     * @return DailySummaryResponse for the specified date
     */
    DailySummaryResponse getSummaryByDate(UUID businessId, LocalDate date);

    /**
     * Logs a daily action for a business.
     * @param businessId UUID of the business
     * @param description Description of the action
     */
    void logDailyAction(UUID businessId, String description);
}
