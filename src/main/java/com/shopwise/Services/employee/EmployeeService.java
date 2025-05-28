package com.shopwise.Services.employee;

import com.shopwise.Dto.employee.CollaboratorInviteRequest;
import com.shopwise.Dto.employee.EmployeeRequest;
import com.shopwise.Dto.employee.EmployeeResponse;
import com.shopwise.Dto.employee.EmployeeUpdateRequest;
import com.shopwise.enums.Role;
import com.shopwise.models.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing employees in the ShopWise system.
 */
public interface EmployeeService {

    /**
     * Adds a new employee to a business.
     * @param businessId UUID of the business
     * @param request Employee details
     * @return Created EmployeeResponse
     */
    EmployeeResponse addEmployee(UUID businessId, EmployeeRequest request);

    /**
     * Updates an existing employee's information.
     * @param employeeId UUID of the employee to update
     * @param request Updated employee details
     * @return Updated EmployeeResponse
     * @deprecated Use updateEmployee(UUID, EmployeeUpdateRequest, User) instead
     */
    @Deprecated
    EmployeeResponse updateEmployee(UUID employeeId, EmployeeUpdateRequest request);
    
    /**
     * Updates an existing employee's information with user authentication.
     * @param employeeId UUID of the employee to update
     * @param request Updated employee details
     * @param currentUser The user performing the update
     * @return Updated EmployeeResponse
     */
    EmployeeResponse updateEmployee(UUID employeeId, EmployeeUpdateRequest request, User currentUser);
    
    /**
     * Gets the business ID associated with an employee.
     * @param employeeId UUID of the employee
     * @return UUID of the business, or null if not found
     */
    UUID getEmployeeBusinessId(UUID employeeId);
    
    /**
     * Checks if a user is the owner of a business.
     * @param businessId UUID of the business
     * @param userId UUID of the user
     * @return true if the user is the owner, false otherwise
     */
    boolean isUserBusinessOwner(UUID businessId, UUID userId);

    /**
     * Disables an employee (soft delete).
     * @param employeeId UUID of the employee to disable
     */
    void disableEmployee(UUID employeeId);

    /**
     * Retrieves all employees for a specific business.
     * @param businessId UUID of the business
     * @return List of EmployeeResponse objects
     */
    List<EmployeeResponse> getEmployeesByBusiness(UUID businessId);

    /**
     * Assigns a role to an employee.
     * @param employeeId UUID of the employee
     * @param role Role to assign
     */
    void assignRole(UUID employeeId, Role role);
    
    /**
     * Checks if a user is authorized to access a business's employee data.
     * @param businessId UUID of the business
     * @param userId UUID of the user
     * @return true if the user is authorized, false otherwise
     */
    boolean isUserAuthorizedForBusiness(UUID businessId, UUID userId);
    
    /**
     * Gets an employee by ID and verifies it belongs to the specified business.
     * @param employeeId UUID of the employee to retrieve
     * @param businessId UUID of the business to verify ownership
     * @return EmployeeResponse if found and belongs to the business
     * @throws EmployeeException if employee not found or doesn't belong to the business
     */
    EmployeeResponse getEmployeeByIdAndBusiness(UUID employeeId, UUID businessId);

    /**
     * Invites a collaborator to join a business.
     * @param businessId UUID of the business
     * @param request Collaborator invitation details
     */
    void inviteCollaborator(UUID businessId, CollaboratorInviteRequest request);

    /**
     * Confirms a collaborator using the invitation token.
     * @param token Invitation token
     */
    void confirmCollaborator(String token);
}
