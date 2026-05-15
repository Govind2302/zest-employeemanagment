package com.zest.test.employeemanagement.service;

import com.zest.test.employeemanagement.dto.LoginRequest;
import com.zest.test.employeemanagement.dto.LoginResponse;
import com.zest.test.employeemanagement.dto.RegisterRequest;
import com.zest.test.employeemanagement.entity.Role;
import com.zest.test.employeemanagement.entity.User;
import com.zest.test.employeemanagement.repository.RoleRepository;
import com.zest.test.employeemanagement.repository.UserRepository;
import com.zest.test.employeemanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // ----------------------------------------------------------------
    // Register new user (always assigned ROLE_USER)
    // ----------------------------------------------------------------
    @Transactional
    public String register(RegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new RuntimeException("Username is required");

        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new RuntimeException("Email is required");

        if (request.getPassword() == null || request.getPassword().length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");

        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already taken");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already registered");

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found in DB. Check seed data."));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return "User registered successfully";
    }

    // ----------------------------------------------------------------
    // Login — validates credentials and returns a single JWT token
    // ----------------------------------------------------------------
    public LoginResponse login(LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new RuntimeException("Username is required");

        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new RuntimeException("Password is required");

        // Spring Security validates credentials — throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponse(token, request.getUsername());
    }
}