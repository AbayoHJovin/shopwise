package com.shopwise.controllers;

import com.shopwise.Dto.Request.UserLoginRequest;
import com.shopwise.Dto.Request.UserRegisterRequest;
import com.shopwise.Dto.UserDto;
import com.shopwise.Services.UserService;
import com.shopwise.Services.auth.JwtService;
import com.shopwise.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get the user details
            User user = userService.getUserByEmailAndPassword(request.getEmail(),request.getPassword());
            
            // Generate JWT token
            String jwt = jwtService.generateToken(user);
            
            // Set the token in an HTTP-only cookie
            Cookie cookie = new Cookie("accessToken", jwt);
            cookie.setSecure(true); // Ensure cookie is only sent over HTTPS
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            cookie.setAttribute("SameSite", "None"); // Required for cross-origin cookies
            response.addCookie(cookie);

            // Return success response
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            
            return ResponseEntity.ok(responseBody);
        } catch (AuthenticationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Registration successful");
            responseBody.put("userId", user.getId().toString());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the authentication from the security context
        SecurityContextHolder.clearContext();
        
        // Clear the authentication cookie
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true); // Ensure cookie is only sent over HTTPS
        cookie.setAttribute("SameSite", "None"); // Required for cross-origin cookies
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logout successful");
        
        return ResponseEntity.ok(responseBody);
    }
    
    /**
     * Endpoint to check if a user is authenticated and return their details
     * This endpoint is used by the frontend to verify authentication status with credentials included
     * 
     * @param request The HTTP request containing cookies
     * @return User details if authenticated, or unauthorized status if not
     */

    @GetMapping("/me")
    @Transactional
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            // Get the token from the cookie
            Cookie[] cookies = request.getCookies();
            String token = null;
            
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            
            if (token == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Authentication required");
                errorResponse.put("message", "Please log in to access this resource");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            try {
                // Validate the token and get user details
                String email = jwtService.extractClaims(token).getSubject(); // Email is stored as the subject
                String userType = jwtService.extractType(token);
                
                if (userType != null && !"user".equals(userType)) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid credentials");
                    errorResponse.put("message", "Your session is invalid. Please log in again.");
                    errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                }
                
                User user = userService.getUserByEmail(email);
                
                if (user == null) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", "User not found");
                    errorResponse.put("message", "Your account could not be found. Please log in again.");
                    errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
                }
                
                // Convert to DTO to avoid exposing sensitive information
                UserDto userDto = UserDto.fromEntity(user);
                
                return ResponseEntity.ok(userDto);
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Session expired");
                errorResponse.put("message", "Your session has expired. Please log in again.");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            } catch (io.jsonwebtoken.JwtException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid token");
                errorResponse.put("message", "Your authentication token is invalid. Please log in again.");
                errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication error");
            errorResponse.put("message", "An error occurred while verifying your identity. Please try again later.");
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("details", e.getMessage()); // Only for debugging, can be removed in production
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
