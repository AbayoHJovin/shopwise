package com.shopwise.Services.employee;

import com.shopwise.Dto.employee.CollaboratorInviteRequest;
import com.shopwise.Dto.employee.EmployeeRequest;
import com.shopwise.Dto.employee.EmployeeResponse;
import com.shopwise.Dto.employee.EmployeeUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.CollaboratorTokenRepository;
import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.dailysummary.DailySummaryService;
import com.shopwise.enums.Role;
import com.shopwise.models.Business;
import com.shopwise.models.CollaboratorToken;
import com.shopwise.models.Employee;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BusinessRepository businessRepository;
    private final CollaboratorTokenRepository collaboratorTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DailySummaryService dailySummaryService;
    
    private static final int TOKEN_EXPIRY_DAYS = 7;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(UUID businessId, EmployeeRequest request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> EmployeeException.notFound("Business not found with ID: " + businessId));
        
        // Check if employee with the same email already exists
        if (employeeRepository.findByEmail(request.getEmail()) != null) {
            throw EmployeeException.conflict("Employee with email " + request.getEmail() + " already exists");
        }
        String randomPassword = UUID.randomUUID().toString();
        // Create new employee
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(randomPassword));
        employee.setSalary(request.getSalary());
        employee.setJoinedDate(LocalDate.now());
        employee.setDisabled(false);
        employee.setCollaborator(request.isCollaborator());
        employee.setEmailConfirmed(true); // Default to true for direct adds
        employee.setRole(Role.MANAGER); // Default role
        employee.setBusiness(business);
        
        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Log the employee addition in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Employee " + savedEmployee.getName() + " (" + savedEmployee.getEmail() + ") was added");
        
        // Return response
        return mapToEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID employeeId, EmployeeUpdateRequest request) {
        // This is the deprecated method without user authentication
        // Call the new method with null user (for backward compatibility)
        return updateEmployee(employeeId, request, null);
    }
    
    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID employeeId, EmployeeUpdateRequest request, User currentUser) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeException.notFound("Employee not found with ID: " + employeeId));
        
        // Get the business
        Business business = employee.getBusiness();
        if (business == null) {
            throw EmployeeException.badRequest("Employee is not associated with any business");
        }
        
        // If currentUser is provided, verify they are the owner of the business
        if (currentUser != null) {
            boolean isOwner = isUserBusinessOwner(business.getId(), currentUser.getId());
            if (!isOwner) {
                throw EmployeeException.forbidden("Only the business owner can update employee information");
            }
        }
        
        // Track if any changes were made
        boolean changesApplied = false;
        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            String oldName = employee.getName();
            employee.setName(request.getName());
            changesApplied = true;
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if the new email is already in use by another employee
            Employee existingEmployee = employeeRepository.findByEmail(request.getEmail());
            if (existingEmployee != null && !existingEmployee.getId().equals(employeeId)) {
                throw EmployeeException.conflict("Email " + request.getEmail() + " is already in use by another employee");
            }
            String oldEmail = employee.getEmail();
            employee.setEmail(request.getEmail());
            changesApplied = true;
        }
        if (request.getSalary() != null) {
            double oldSalary = employee.getSalary();
            employee.setSalary(request.getSalary());
            changesApplied = true;
        }
        
        if (request.getIsDisabled() != null) {
            boolean oldStatus = employee.isDisabled();
            employee.setDisabled(request.getIsDisabled());
            changesApplied = true;
        }
        
        if (request.getIsCollaborator() != null) {
            boolean oldCollaborator = employee.isCollaborator();
            employee.setCollaborator(request.getIsCollaborator());
            changesApplied = true;
        }
        
        if (request.getRole() != null) {
            Role oldRole = employee.getRole();
            employee.setRole(request.getRole());
            changesApplied = true;
        }
        
        // If no changes were applied, return the existing employee
        if (!changesApplied) {
            return mapToEmployeeResponse(employee);
        }
        
        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(employee);

        // Log the employee update in daily summary
        dailySummaryService.logDailyAction(updatedEmployee.getBusiness().getId(), 
                "Employee " + updatedEmployee.getName() + " (" + updatedEmployee.getEmail() + ") was updated");
        
        // Return response
        return mapToEmployeeResponse(updatedEmployee);
    }
    
    @Override
    public UUID getEmployeeBusinessId(UUID employeeId) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null || employee.getBusiness() == null) {
            return null;
        }
        return employee.getBusiness().getId();
    }
    
    @Override
    public boolean isUserBusinessOwner(UUID businessId, UUID userId) {
        // Find the business
        Business business = businessRepository.findById(businessId).orElse(null);
        if (business == null) {
            return false;
        }
        
        // Get the user
        User user = null;
        try {
            user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        
        // Check if the user is the owner of the business
        // The owner is defined as the first collaborator added to the business
        return isBusinessOwner(business, user);
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

    @Override
    @Transactional
    public void disableEmployee(UUID employeeId) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeException.notFound("Employee not found with ID: " + employeeId));
        
        // Check if already disabled
        if (employee.isDisabled()) {
            throw EmployeeException.badRequest("Employee is already disabled");
        }
        
        // Disable employee
        employee.setDisabled(true);
        
        // Save updated employee
        employeeRepository.save(employee);
        
        // Log the employee disabling in daily summary
        dailySummaryService.logDailyAction(employee.getBusiness().getId(), 
                "Employee " + employee.getName() + " (" + employee.getEmail() + ") was disabled");
    }

    @Override
    public List<EmployeeResponse> getEmployeesByBusiness(UUID businessId) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> EmployeeException.notFound("Business not found with ID: " + businessId));
        
        // Get all employees for the business
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(employee -> employee.getBusiness() != null && 
                        employee.getBusiness().getId().equals(businessId))
                .collect(Collectors.toList());
        
        // Map to response DTOs
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignRole(UUID employeeId, Role role) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeException.notFound("Employee not found with ID: " + employeeId));
        
        // Check if employee is disabled
        if (employee.isDisabled()) {
            throw EmployeeException.badRequest("Cannot assign role to a disabled employee");
        }
        
        // Assign role
        employee.setRole(role);
        
        // Save updated employee
        employeeRepository.save(employee);
        
        // Log the role assignment in daily summary
        dailySummaryService.logDailyAction(employee.getBusiness().getId(), 
                "Employee " + employee.getName() + " (" + employee.getEmail() + ") was assigned the role of " + role.name());
    }

    @Override
    @Transactional
    public void inviteCollaborator(UUID businessId, CollaboratorInviteRequest request) {
        // Find the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> EmployeeException.notFound("Business not found with ID: " + businessId));
        
        // Check if employee with the same email already exists
        if (employeeRepository.findByEmail(request.getEmail()) != null) {
            throw EmployeeException.conflict("Employee with email " + request.getEmail() + " already exists");
        }
        
        // Check if there's an existing invitation
        Optional<CollaboratorToken> existingToken = collaboratorTokenRepository.findByEmailAndBusiness(
                request.getEmail(), business);
        
        if (existingToken.isPresent()) {
            throw EmployeeException.conflict("An invitation has already been sent to " + request.getEmail());
        }
        
        // Create new token
        CollaboratorToken token = new CollaboratorToken();
        token.setEmail(request.getEmail());
        token.setName(request.getName());
        token.setMessage(request.getMessage());
        token.setBusiness(business);
        token.setToken(generateUniqueToken());
        token.setExpiryDate(LocalDateTime.now().plusDays(TOKEN_EXPIRY_DAYS));
        
        // Save token
        collaboratorTokenRepository.save(token);
        
        // Log the collaborator invitation in daily summary
        dailySummaryService.logDailyAction(businessId, 
                "Collaboration invitation sent to " + request.getName() + " (" + request.getEmail() + ")");
        
        // In a real application, we would send an email with the invitation link here
    }

    @Override
    @Transactional
    public void confirmCollaborator(String token) {
        // Find valid token
        CollaboratorToken collaboratorToken = collaboratorTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> EmployeeException.badRequest("Invalid or expired invitation token"));
        
        // Create new employee as collaborator
        Employee employee = new Employee();
        employee.setName(collaboratorToken.getName());
        employee.setEmail(collaboratorToken.getEmail());
        employee.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Generate random password
        employee.setSalary(0.0); // No salary for collaborators
        employee.setJoinedDate(LocalDate.now());
        employee.setDisabled(false);
        employee.setCollaborator(true);
        employee.setEmailConfirmed(true);
        employee.setRole(Role.MANAGER); // Default role
        employee.setBusiness(collaboratorToken.getBusiness());
        
        // Save employee
        employeeRepository.save(employee);
        
        // Delete token
        collaboratorTokenRepository.delete(collaboratorToken);
        
        // Log the collaborator confirmation in daily summary
        dailySummaryService.logDailyAction(employee.getBusiness().getId(), 
                "Collaborator " + employee.getName() + " (" + employee.getEmail() + ") joined the business");
        
        // In a real application, we would send an email with the password reset link here
    }
    
    // Helper methods
    
    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .salary(employee.getSalary())
                .joinedDate(employee.getJoinedDate())
                .isDisabled(employee.isDisabled())
                .isCollaborator(employee.isCollaborator())
                .emailConfirmed(employee.isEmailConfirmed())
                .role(employee.getRole())
                .businessId(employee.getBusiness().getId())
                .businessName(employee.getBusiness().getName())
                .build();
    }
    
    private String generateUniqueToken() {
        // In a real application, use a more secure token generation method
        return UUID.randomUUID().toString();
    }
    
    @Override
    public boolean isUserAuthorizedForBusiness(UUID businessId, UUID userId) {
        // Check if business exists
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> EmployeeException.notFound("Business not found with ID: " + businessId));
        
        // Check if user is a collaborator of the business
        return businessRepository.isUserCollaborator(businessId, userId);
    }
    
    @Override
    public EmployeeResponse getEmployeeByIdAndBusiness(UUID employeeId, UUID businessId) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeException.notFound("Employee not found with ID: " + employeeId));
        
        // Verify the employee belongs to the specified business
        if (employee.getBusiness() == null || !employee.getBusiness().getId().equals(businessId)) {
            throw EmployeeException.unauthorized("Employee does not belong to the specified business");
        }
        
        // Return the employee DTO
        return mapToEmployeeResponse(employee);
    }
}
