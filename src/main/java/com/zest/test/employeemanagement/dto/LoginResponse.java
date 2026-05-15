package com.zest.test.employeemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String tokenType = "Bearer";

    // Convenience constructor (token + username, tokenType defaults to Bearer)
    public LoginResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}