package com.shopwise.security;

import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.Services.auth.JwtService;
import com.shopwise.models.User;
import com.shopwise.models.Employee;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractTokenFromCookie(request);

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.extractClaims(jwt);
                String id = claims.get("id", String.class);
                String type = claims.get("type", String.class);

                if ("user".equals(type)) {
                    Optional<User> userOptional = userRepository.findById(java.util.UUID.fromString(id));
                    userOptional.ifPresent(user -> {
                        // Create authority based on user role
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name());
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                user, null, Collections.singletonList(authority)
                        );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                } else if ("employee".equals(type)) {
                    Optional<Employee> employeeOptional = employeeRepository.findById(java.util.UUID.fromString(id));
                    employeeOptional.ifPresent(employee -> {
                        // Create authority based on employee role
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                                "ROLE_" + employee.getRole().name());
                        
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                employee, null, Collections.singletonList(authority)
                        );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }

            } catch (Exception e) {
                System.out.println("JWT processing failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
