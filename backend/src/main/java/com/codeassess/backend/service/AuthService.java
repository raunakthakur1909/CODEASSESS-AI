package com.codeassess.backend.service;

import com.codeassess.backend.dto.LoginRequest;
import com.codeassess.backend.dto.LoginResponse;
import com.codeassess.backend.dto.RegisterRequest;
import com.codeassess.backend.model.Role;
import com.codeassess.backend.model.User;
import com.codeassess.backend.repository.UserRepository;
import com.codeassess.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Register a new user.
     * 
     * @param registerRequest the registration details
     * @throws RuntimeException if email already exists
     */
    public void registerUser(RegisterRequest registerRequest) {
        // Validate password policy: at least 8 chars, 1 uppercase, 1 digit
        String password = registerRequest.getPassword();
        if (password == null || password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            throw new com.codeassess.backend.exception.BusinessException("Password must be at least 8 characters long and contain at least one uppercase letter and one digit");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            // Log it securely but return immediately to prevent enumeration
            org.slf4j.LoggerFactory.getLogger(AuthService.class).warn("Registration attempt with existing email: {}", registerRequest.getEmail());
            return;
        }

        // Create new user
        User user = new User();
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        
        // Hash the password using BCrypt
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        user.setPassword(hashedPassword);
        
        // Default role is CANDIDATE
        user.setRole(Role.CANDIDATE);

        // Save to database
        userRepository.save(user);
    }

    /**
     * Login user and generate JWT token.
     * 
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token
     * @throws RuntimeException if credentials are invalid
     */
    public LoginResponse loginUser(LoginRequest loginRequest) {
        // Find user by email
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        // Return login response
        return new LoginResponse(token, jwtUtil.getExpirationInSeconds());
    }
}
