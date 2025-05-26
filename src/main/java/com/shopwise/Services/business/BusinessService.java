package com.shopwise.Services.business;

import com.shopwise.Dto.BusinessDto;
import com.shopwise.Dto.Request.CreateBusinessRequest;
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
}
