package com.zest.test.employeemanagement.config;

import com.zest.test.employeemanagement.security.JwtAuthFilter;
import com.zest.test.employeemanagement.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // ----------------------------------------------------------------
    // Main security filter chain
    // ----------------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Apply CORS config defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Disable CSRF — not needed for stateless JWT APIs
                .csrf(csrf -> csrf.disable())

                // 3. Route-level authorization
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints — no token needed
                        .requestMatchers("/api/auth/**").permitAll()

                        // Employee endpoints — any authenticated user
                        .requestMatchers("/api/employees/**").authenticated()

                        // Everything else — must be authenticated
                        .anyRequest().authenticated()
                )

                // 4. Stateless session — JWT handles state, not server
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Use our custom UserDetailsService + BCrypt
                .authenticationProvider(authenticationProvider())

                // 6. Run JwtAuthFilter BEFORE Spring's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ----------------------------------------------------------------
    // CORS — allow frontend (React / Postman) to call the API
    // ----------------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins — add your frontend URL here in production
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",   // React dev server
                "http://localhost:5173",   // Vite dev server
                "http://localhost:8080"    // same-origin fallback
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers — Authorization header must be included for JWT
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // Expose Authorization header to frontend JS
        config.setExposedHeaders(List.of("Authorization"));

        // Allow cookies / credentials if needed
        config.setAllowCredentials(true);

        // Cache preflight response for 1 hour (reduces OPTIONS calls)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // apply to all routes
        return source;
    }

    // ----------------------------------------------------------------
    // Authentication provider — wires UserDetailsService + BCrypt
    // ----------------------------------------------------------------
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ----------------------------------------------------------------
    // AuthenticationManager — used in AuthService for login
    // ----------------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ----------------------------------------------------------------
    // BCrypt password encoder
    // ----------------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);  // strength=10, same as DB seed
    }
}