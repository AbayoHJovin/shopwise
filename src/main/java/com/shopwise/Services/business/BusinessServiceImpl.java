package com.shopwise.Services.business;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.LocationDto;
import com.shopwise.Dto.Request.CreateBusinessRequest;
import com.shopwise.Dto.business.BusinessDeleteRequest;
import com.shopwise.Dto.business.BusinessUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.CollaborationRequestRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.UserService;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.models.Business;
import com.shopwise.models.CollaborationRequest;
import com.shopwise.models.Location;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository  businessRepository;
    private final UserRepository userRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final DailySummaryService dailySummaryService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_DAYS = 7;

    @Override
    @Transactional
    public BusinessDto createBusiness(CreateBusinessRequest request, User owner) {
        Business business = new Business();
        business.setName(request.getName());
        
        // Convert LocationDto to Location entity
        Location location = new Location();
        if (request.getLocation() != null) {
            location.setProvince(request.getLocation().getProvince());
            location.setDistrict(request.getLocation().getDistrict());
            location.setSector(request.getLocation().getSector());
            location.setCell(request.getLocation().getCell());
            location.setVillage(request.getLocation().getVillage());
            location.setLatitude(request.getLocation().getLatitude());
            location.setLongitude(request.getLocation().getLongitude());
        }
        business.setLocation(location);
        
        business.setAbout(request.getAbout());
        business.setWebsiteLink(request.getWebsiteLink());
        
        // Initialize collections
        business.setCollaborators(new ArrayList<>());
        business.getCollaborators().add(owner);
        
        business.setProducts(new ArrayList<>());
        business.setEmployees(new ArrayList<>());
        business.setExpenses(new ArrayList<>());
        business.setSaleRecords(new ArrayList<>());
        business.setSummaries(new ArrayList<>());
        business.setCollaborationRequests(new ArrayList<>());

        Business savedBusiness = businessRepository.save(business);
        
        dailySummaryService.logDailyAction(savedBusiness.getId(),
                "Business '" + savedBusiness.getName() + "' was created by " + owner.getEmail());
        
        return mapToDto(savedBusiness);
    }

    @Override
    @Transactional
    public BusinessDto getBusinessById(UUID businessId, User requester) {
        Business business = findBusinessAndCheckAccess(businessId, requester);
        return mapToDto(business);
    }

    @Override
    @Transactional
    public List<BusinessDto> getBusinessesForUser(User user) {
        List<Business> businesses = businessRepository.findBusinessesByCollaborator(user);
        return businesses.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void inviteCollaborator(UUID businessId, String email, User owner) {
        Business business = findBusinessAndCheckAccess(businessId, owner);
        
        // Check if email is already a collaborator
        boolean isAlreadyCollaborator = business.getCollaborators().stream()
                .anyMatch(user -> user.getEmail().equals(email));
        
        if (isAlreadyCollaborator) {
            throw BusinessException.badRequest("User with email " + email + " is already a collaborator");
        }
        
        // Check if there's an existing invitation
        collaborationRequestRepository.findByEmailAndBusiness(email, business)
                .ifPresent(request -> {
                    throw BusinessException.badRequest("An invitation has already been sent to " + email);
                });
        
        // Create new collaboration request
        CollaborationRequest request = new CollaborationRequest();
        request.setEmail(email);
        request.setBusiness(business);
        request.setToken(generateUniqueToken());
        request.setExpiryDate(LocalDateTime.now().plusDays(TOKEN_EXPIRY_DAYS));
        
        collaborationRequestRepository.save(request);
        
        // Log the invitation in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Collaboration invitation sent to " + email + " by " + owner.getEmail());
        
        // In a real application, we would send an email with the invitation link here
    }

    @Override
    @Transactional
    public void acceptCollaboration(String token, User invitee) {
        CollaborationRequest request = collaborationRequestRepository
                .findValidTokenRequest(token, LocalDateTime.now())
                .orElseThrow(() -> BusinessException.badRequest("Invalid or expired invitation token"));
        
        // Verify the email matches
        if (!request.getEmail().equals(invitee.getEmail())) {
            throw BusinessException.forbidden("This invitation is not for your account");
        }
        
        Business business = request.getBusiness();
        
        // Add user as collaborator if not already added
        if (!business.getCollaborators().contains(invitee)) {
            business.getCollaborators().add(invitee);
            businessRepository.save(business);
            
            // Log the collaboration acceptance in daily summary
            dailySummaryService.logDailyAction(business.getId(), 
                    invitee.getEmail() + " joined as a collaborator");
        }
        
        // Remove the request
        collaborationRequestRepository.delete(request);
    }

    @Override
    @Transactional
    public BusinessDto updateBusiness(UUID businessId, BusinessUpdateRequest updates, User requester) {
        Business business = findBusinessAndCheckAccess(businessId, requester);
        
        // Update fields if provided
        if (updates.getName() != null && !updates.getName().isBlank()) {
            business.setName(updates.getName());
        }
        
        if (updates.getLocation() != null) {
            // Convert LocationDto to Location entity
            Location location = business.getLocation();
            if (location == null) {
                location = new Location();
            }
            
            // Update location fields if provided in the DTO
            LocationDto locationDto = updates.getLocation();
            if (locationDto.getProvince() != null) {
                location.setProvince(locationDto.getProvince());
            }
            if (locationDto.getDistrict() != null) {
                location.setDistrict(locationDto.getDistrict());
            }
            if (locationDto.getSector() != null) {
                location.setSector(locationDto.getSector());
            }
            if (locationDto.getCell() != null) {
                location.setCell(locationDto.getCell());
            }
            if (locationDto.getVillage() != null) {
                location.setVillage(locationDto.getVillage());
            }
            if (locationDto.getLatitude() != null) {
                location.setLatitude(locationDto.getLatitude());
            }
            if (locationDto.getLongitude() != null) {
                location.setLongitude(locationDto.getLongitude());
            }
            
            business.setLocation(location);
        }
        
        if (updates.getAbout() != null) {
            business.setAbout(updates.getAbout());
        }
        
        if (updates.getWebsiteLink() != null) {
            business.setWebsiteLink(updates.getWebsiteLink());
        }
        
        Business updatedBusiness = businessRepository.save(business);
        
        // Log the business update in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Business information updated by " + requester.getEmail());
        
        return mapToDto(updatedBusiness);
    }

    @Override
    public List<User> listCollaborators(UUID businessId, User requester) {
        Business business = findBusinessAndCheckAccess(businessId, requester);
        return new ArrayList<>(business.getCollaborators());
    }

    @Override
    @Transactional
    public void removeCollaborator(UUID businessId, UUID userId, User requester) {
        Business business = findBusinessAndCheckAccess(businessId, requester);
        
        // Check if requester is trying to remove themselves
        if (requester.getId().equals(userId)) {
            throw BusinessException.badRequest("You cannot remove yourself from the business");
        }
        
        // Find the user to remove
        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("User not found"));
        
        // Check if the user is actually a collaborator
        if (!business.getCollaborators().contains(userToRemove)) {
            throw BusinessException.badRequest("User is not a collaborator of this business");
        }
        
        // Remove the collaborator
        business.getCollaborators().remove(userToRemove);
        businessRepository.save(business);
        
        // Log the collaborator removal in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Collaborator " + userToRemove.getEmail() + " was removed by " + requester.getEmail());
    }
    
    // Helper methods
    
    private Business findBusinessAndCheckAccess(UUID businessId, User requester) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> BusinessException.notFound("Business not found"));
        
        // Check if user has access to this business by comparing IDs
        boolean hasAccess = false;
        UUID requesterId = requester.getId();
        
        // Extract all collaborator IDs and check if the requester's ID is among them
        if (business.getCollaborators() != null) {
            hasAccess = business.getCollaborators().stream()
                    .map(User::getId)
                    .anyMatch(id -> id.equals(requesterId));
        }
        
        if (!hasAccess) {
            // Log the IDs for debugging
            System.out.println("BusinessId: " + businessId);
            System.out.println("RequesterId: " + requesterId);
            
            // Log all collaborator IDs for debugging
            if (business.getCollaborators() != null) {
                System.out.println("Collaborator IDs: " + 
                    business.getCollaborators().stream()
                        .map(User::getId)
                        .map(UUID::toString)
                        .collect(java.util.stream.Collectors.joining(", ")));
            } else {
                System.out.println("Collaborators list is null");
            }
            
            throw BusinessException.forbidden("You don't have access to this business");
        }
        
        return business;
    }
    
    /**
     * Checks if the user is the owner of the business
     * The owner is defined as the first collaborator added to the business
     * 
     * @param business The business to check ownership for
     * @param user The user to check if they are the owner
     * @return true if the user is the owner, false otherwise
     */
    private boolean isBusinessOwner(Business business, User user) {
        if (business.getCollaborators() == null || business.getCollaborators().isEmpty()) {
            return false;
        }
        
        // The owner is the first collaborator in the list
        // This assumes the first collaborator added is the owner
        User owner = business.getCollaborators().get(0);
        return owner.getId().equals(user.getId());
    }
    
    private BusinessDto mapToDto(Business business) {
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
        
        return BusinessDto.builder()
                .id(business.getId())
                .name(business.getName())
                .location(locationDto)
                .about(business.getAbout())
                .websiteLink(business.getWebsiteLink())
                .collaboratorIds(business.getCollaborators().stream()
                        .map(User::getId)
                        .collect(Collectors.toList()))
                .productCount(business.getProducts() != null ? business.getProducts().size() : 0)
                .employeeCount(business.getEmployees() != null ? business.getEmployees().size() : 0)
                .build();
    }
    
    private String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }
    
    @Override
    @Transactional
    public String deleteBusiness(UUID businessId, BusinessDeleteRequest deleteRequest, User requester) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> BusinessException.notFound("Business not found"));
        
        // Verify that the requester is the owner of the business
        if (!isBusinessOwner(business, requester)) {
            throw BusinessException.forbidden("Only the business owner can delete the business");
        }
        
        // Verify the user's password
        if (!passwordEncoder.matches(deleteRequest.getPassword(), requester.getPassword())) {
            throw BusinessException.forbidden("Invalid password. Please enter your correct password to confirm deletion.");
        }
        
        // Verify confirmation text if provided
        if (deleteRequest.getConfirmationText() != null && !deleteRequest.getConfirmationText().isEmpty()) {
            String expectedConfirmation = "DELETE " + business.getName();
            if (!expectedConfirmation.equals(deleteRequest.getConfirmationText())) {
                throw BusinessException.badRequest("Confirmation text does not match. Please type 'DELETE " + business.getName() + "' to confirm.");
            }
        }
        
        // Log the deletion action before deleting the business
        String businessName = business.getName();
        UUID ownerId = requester.getId();
        String ownerEmail = requester.getEmail();
        
        // Perform the deletion
        businessRepository.delete(business);
        
        // Return success message
        return "Business '" + businessName + "' has been permanently deleted.";
    }
}
