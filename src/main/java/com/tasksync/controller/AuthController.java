package com.tasksync.controller;

import com.tasksync.dto.AuthRequest;
import com.tasksync.dto.AuthResponse;
import com.tasksync.dto.RegisterRequest;

import com.tasksync.service.AuthService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ----------------- REGISTER ------------------
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "User registered successfully";
    }

    // ----------------- LOGIN ------------------
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    // ----------------- REFRESH TOKEN ------------------
    @PostMapping("/refresh")
    public String refresh(@RequestParam String refreshToken) {
        return authService.refreshAccessToken(refreshToken);
    }
}
