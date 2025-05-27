package com.shopwise.Services.business;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.Request.CreateBusinessRequest;
import com.shopwise.Dto.business.BusinessDeleteRequest;
import com.shopwise.Dto.business.BusinessUpdateRequest;
import com.shopwise.models.User;

import java.util.List;
import java.util.UUID;

public interface BusinessService {
    BusinessDto createBusiness(CreateBusinessRequest request, User owner);
    BusinessDto getBusinessById(UUID businessId, User requester);
    List<BusinessDto> getBusinessesForUser(User user);
    void inviteCollaborator(UUID businessId, String email, User owner);
    void acceptCollaboration(String token, User invitee);
    BusinessDto updateBusiness(UUID businessId, BusinessUpdateRequest updates, User requester);
    List<User> listCollaborators(UUID businessId, User requester);
    void removeCollaborator(UUID businessId, UUID userId, User requester);
    
    /**
     * Deletes a business if the requester is the owner and password verification succeeds
     * 
     * @param businessId The ID of the business to delete
     * @param deleteRequest The deletion request containing password for verification
     * @param requester The user requesting the deletion
     * @return A message confirming the deletion
     * @throws BusinessException if the requester is not the owner or password verification fails
     */
    String deleteBusiness(UUID businessId, BusinessDeleteRequest deleteRequest, User requester);
}
