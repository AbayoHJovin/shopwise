package com.shopwise.Dto.Request;

import com.shopwise.Dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessRequest {
    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Location is required")
    @Valid
    private LocationDto location;
    
    @Size(max = 500, message = "About section must not exceed 500 characters")
    private String about;
    
    private String websiteLink;
}
