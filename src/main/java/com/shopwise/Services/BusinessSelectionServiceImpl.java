package com.shopwise.Services;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.LocationDto;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the BusinessSelectionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessSelectionServiceImpl implements BusinessSelectionService {

    private final com.shopwise.Repository.UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final DailySummaryService dailySummaryService;

    /**
     * Select a business for a user and set it as the active business
     *
     * @param userEmail The email of the user
     * @param businessId The ID of the business to select
     * @param response HTTP response to set the cookie
     * @return true if successful, false otherwise
     * @throws IllegalArgumentException if the business is not found or not owned by the user
     */
    @Override
    @Transactional
    public boolean selectBusinessForUser(String userEmail, UUID businessId, HttpServletResponse response) {
        // Find the user
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"));

        // Use the repository method to efficiently check if user is a collaborator
        boolean isCollaborator = businessRepository.isUserCollaborator(businessId, user.getId());
        
        // If user is not a collaborator, they don't have access
        if (!isCollaborator) {
            throw new IllegalArgumentException("You do not have permission to select this business");
        }

        // Set the business cookie
        Cookie cookie = new Cookie("selectedBusiness", businessId.toString());
        cookie.setHttpOnly(true); // For security, prevent JavaScript access
        cookie.setPath("/");
        cookie.setSecure(true); // Ensure cookie is only sent over HTTPS
        cookie.setAttribute("SameSite", "None"); // Required for cross-origin cookies
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        response.addCookie(cookie);


        log.info("User {} selected business {}", userEmail, business.getName());
        return true;
    }

    /**
     * Get the businesses owned by a user
     *
     * @param userEmail The email of the user
     * @return List of businesses owned by the user
     */
    @Override
    @Transactional(readOnly = true)
    public List<BusinessDto> getBusinessesForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if(user == null) {
            return List.of();
        }
        List<Business> userBusinesses = businessRepository.findBusinessesByCollaborator(user);
        
        return userBusinesses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert a Business entity to a BusinessDto
     *
     * @param business The Business entity
     * @return BusinessDto
     */
    private BusinessDto convertToDto(Business business) {
        // Convert Location entity to LocationDto
        LocationDto locationDto = null;
        if (business.getLocation() != null) {
            locationDto = LocationDto.builder()
                    .province(business.getLocation().getProvince())
                    .district(business.getLocation().getDistrict())
                    .sector(business.getLocation().getSector())
                    .cell(business.getLocation().getCell())
                    .village(business.getLocation().getVillage())
                    .latitude(business.getLocation().getLatitude())
                    .longitude(business.getLocation().getLongitude())
                    .build();
        }
        
        // Get the first collaborator as the owner (if any)
        String ownerEmail = null;
        if (business.getCollaborators() != null && !business.getCollaborators().isEmpty()) {
            ownerEmail = business.getCollaborators().get(0).getEmail();
        }
        
        // Build the BusinessDto using the builder pattern
        return BusinessDto.builder()
                .id(business.getId())
                .name(business.getName())
                .location(locationDto)
                .about(business.getAbout())
                .websiteLink(business.getWebsiteLink())
                .collaboratorIds(business.getCollaborators() != null ?
                        business.getCollaborators().stream()
                                .map(User::getId)
                                .collect(Collectors.toList()) : 
                        List.of())
                .productCount(business.getProducts() != null ? business.getProducts().size() : 0)
                .employeeCount(business.getEmployees() != null ? business.getEmployees().size() : 0)
                .build();
    }
}
