package com.shopwise.Services;

import com.shopwise.Dto.Request.UserRegisterRequest;
import com.shopwise.Repository.UserRepository;
import com.shopwise.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        UUID userId = UUID.randomUUID();
        User user = new User(userId, request.getName(), request.getPhone(), request.getEmail(), 
                passwordEncoder.encode(request.getPassword())); // Ensure password is encoded
        user.setRole(User.UserRoles.USER);
        return userRepository.save(user);
    }

    public User getUserByEmailAndPassword(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password.");
        }
        
        return user;
    }
    
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        return user;
    }
}

