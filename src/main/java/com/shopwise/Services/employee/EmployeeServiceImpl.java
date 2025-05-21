package com.shopwise.Services.employee;

import com.shopwise.Dto.employee.CollaboratorInviteRequest;
import com.shopwise.Dto.employee.EmployeeRequest;
import com.shopwise.Dto.employee.EmployeeResponse;
import com.shopwise.Dto.employee.EmployeeUpdateRequest;
import com.shopwise.Repository.BusinessRepository;
import com.shopwise.Repository.CollaboratorTokenRepository;
import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.enums.Role;
import com.shopwise.models.Business;
import com.shopwise.models.CollaboratorToken;
import com.shopwise.models.Employee;
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
    private final PasswordEncoder passwordEncoder;
    
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
        
        // Create new employee
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPassword(passwordEncoder.encode(request.getPassword()));
        employee.setSalary(request.getSalary());
        employee.setJoinedDate(LocalDate.now());
        employee.setDisabled(false);
        employee.setCollaborator(request.isCollaborator());
        employee.setEmailConfirmed(true); // Default to true for direct adds
        employee.setRole(Role.MANAGER); // Default role
        employee.setBusiness(business);
        
        // Save employee
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Return response
        return mapToEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID employeeId, EmployeeUpdateRequest request) {
        // Find the employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> EmployeeException.notFound("Employee not found with ID: " + employeeId));
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            employee.setName(request.getName());
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            // Check if the new email is already in use by another employee
            Employee existingEmployee = employeeRepository.findByEmail(request.getEmail());
            if (existingEmployee != null && !existingEmployee.getId().equals(employeeId)) {
                throw EmployeeException.conflict("Email " + request.getEmail() + " is already in use by another employee");
            }
            employee.setEmail(request.getEmail());
        }
        
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        if (request.getSalary() != null) {
            employee.setSalary(request.getSalary());
        }
        
        if (request.getIsCollaborator() != null) {
            employee.setCollaborator(request.getIsCollaborator());
        }
        
        // Save updated employee
        Employee updatedEmployee = employeeRepository.save(employee);
        
        // Return response
        return mapToEmployeeResponse(updatedEmployee);
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
}
