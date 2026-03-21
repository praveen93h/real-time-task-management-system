package com.taskmanagement.controller;

import com.taskmanagement.dto.auth.AuthResponse;
import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.RefreshTokenRequest;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        AuthResponse response = authService.getCurrentUserInfo();
        return ResponseEntity.ok(response);
    }
}
