package com.zest.test.employeemanagement.service;

import com.zest.test.employeemanagement.dto.LoginRequest;
import com.zest.test.employeemanagement.dto.LoginResponse;
import com.zest.test.employeemanagement.dto.RegisterRequest;
import com.zest.test.employeemanagement.entity.RefreshToken;
import com.zest.test.employeemanagement.entity.Role;
import com.zest.test.employeemanagement.entity.User;
import com.zest.test.employeemanagement.repository.RefreshTokenRepository;
import com.zest.test.employeemanagement.repository.RoleRepository;
import com.zest.test.employeemanagement.repository.UserRepository;
import com.zest.test.employeemanagement.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.refresh.expiration.days}")
    private long refreshExpirationDays;

    // ----------------------------------------------------------------
    // Register new user (always assigned ROLE_USER)
    // ----------------------------------------------------------------
    @Transactional
    public String register(RegisterRequest request) {

        // Manual validation (no @Valid since we skipped that dep)
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
    // Login — returns access token + refresh token
    // ----------------------------------------------------------------
    @Transactional
    public LoginResponse login(LoginRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank())
            throw new RuntimeException("Username is required");

        if (request.getPassword() == null || request.getPassword().isBlank())
            throw new RuntimeException("Password is required");

        // Spring Security validates credentials — throws exception if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // Generate access token
        String accessToken = jwtUtil.generateToken(userDetails);

        // Generate refresh token and persist it
        String refreshToken = createRefreshToken(request.getUsername());

        return new LoginResponse(accessToken, refreshToken, request.getUsername(), "Bearer");
    }

    // ----------------------------------------------------------------
    // Refresh — exchange refresh token for new access token
    // ----------------------------------------------------------------
    @Transactional
    public LoginResponse refresh(String refreshToken) {

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        UserDetails userDetails = userDetailsService
                .loadUserByUsername(stored.getUser().getUsername());

        String newAccessToken = jwtUtil.generateToken(userDetails);

        return new LoginResponse(newAccessToken, refreshToken,
                stored.getUser().getUsername(), "Bearer");
    }

    // ----------------------------------------------------------------
    // Logout — delete refresh token from DB (invalidates it)
    // ----------------------------------------------------------------
    @Transactional
    public String logout(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenRepository.deleteByUser(user);
        return "Logged out successfully";
    }

    // ----------------------------------------------------------------
    // Internal — create and persist refresh token for a user
    // ----------------------------------------------------------------
    private String createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete existing refresh token if any (one active token per user)
        refreshTokenRepository.findByUser(user)
                .ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString()); // random UUID as refresh token
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(refreshExpirationDays));

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }
}