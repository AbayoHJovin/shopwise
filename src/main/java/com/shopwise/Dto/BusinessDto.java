package com.shopwise.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDto {
    private UUID id;
    private String name;
    private LocationDto location;
    private String about;
    private String websiteLink;
    private List<UUID> collaboratorIds;
    private int productCount;
    private int employeeCount;
}
