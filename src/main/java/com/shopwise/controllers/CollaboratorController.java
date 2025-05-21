package com.shopwise.controllers;

import com.shopwise.Services.employee.EmployeeException;
import com.shopwise.Services.employee.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborators")
@RequiredArgsConstructor
public class CollaboratorController {

    private final EmployeeService employeeService;

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmCollaborator(@RequestParam String token) {
        try {
            employeeService.confirmCollaborator(token);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Collaboration confirmed successfully");
            return ResponseEntity.ok(response);
        } catch (EmployeeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
