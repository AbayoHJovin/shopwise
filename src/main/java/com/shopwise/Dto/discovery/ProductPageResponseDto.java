package com.shopwise.Dto.discovery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for paginated product responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPageResponseDto {
    private List<PublicProductDto> products;
    private long totalCount;
    private int skip;
    private int limit;
    private boolean hasMore;
    private String sortBy;
    private String sortDirection;
    private String businessName;
}
