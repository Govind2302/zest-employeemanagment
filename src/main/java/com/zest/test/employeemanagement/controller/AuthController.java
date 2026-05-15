package com.zest.test.employeemanagement.controller;

import com.zest.test.employeemanagement.dto.LoginRequest;
import com.zest.test.employeemanagement.dto.LoginResponse;
import com.zest.test.employeemanagement.dto.RegisterRequest;
import com.zest.test.employeemanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ----------------------------------------------------------------
    // POST /api/auth/register
    // Body: { "username": "", "email": "", "password": "" }
    // ----------------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestBody RegisterRequest request) {

        String message = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }

    // ----------------------------------------------------------------
    // POST /api/auth/login
    // Body: { "username": "", "password": "" }
    // Response: { "token": "...", "username": "...", "tokenType": "Bearer" }
    // ----------------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // GET /api/auth/me
    // Requires: Authorization: Bearer <token>
    // Returns currently logged-in user info from the token
    // ----------------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(Map.of(
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities()
        ));
    }
}