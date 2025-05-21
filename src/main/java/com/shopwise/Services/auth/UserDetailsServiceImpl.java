package com.shopwise.Services.auth;

import com.shopwise.Repository.EmployeeRepository;
import com.shopwise.Repository.UserRepository;
import com.shopwise.models.Employee;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find user first
        User  userOpt= userRepository.findByEmail(email);
        if (userOpt !=null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userOpt.getRole().name());

            return new org.springframework.security.core.userdetails.User(
                    userOpt.getEmail(),
                    userOpt.getPassword(),
                    Collections.singletonList(authority)
            );
        }

        // If user not found, try to find employee
        Employee employeeOpt = employeeRepository.findByEmail(email);
        if (employeeOpt !=null) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + employeeOpt.getRole().name());

            return new org.springframework.security.core.userdetails.User(
                    employeeOpt.getEmail(),
                    employeeOpt.getPassword(),
                    Collections.singletonList(authority)
            );
        }
        throw new UsernameNotFoundException("User or Employee not found with email: " + email);
    }

}
