package com.tasksync.service;

import com.tasksync.dto.AuthRequest;
import com.tasksync.dto.AuthResponse;
import com.tasksync.dto.RegisterRequest;
import com.tasksync.entity.RefreshToken;
import com.tasksync.entity.Role;
import com.tasksync.entity.User;
import com.tasksync.repository.RefreshTokenRepository;
import com.tasksync.repository.RoleRepository;
import com.tasksync.repository.UserRepository;
import com.tasksync.security.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // ----------------- REGISTER ------------------
    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role defaultRole = roleRepository
                .findByName(request.getRole() != null ? request.getRole() : "ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(defaultRole));

        return userRepository.save(user);
    }

    // ----------------- LOGIN ------------------
    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String accessToken = jwtUtil.generateToken(request.getUsername());
        String refreshToken = generateRefreshToken(request.getUsername());

        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    // ----------------- CREATE REFRESH TOKEN ------------------
    @Transactional
    public String generateRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 7)); // 7 days

        refreshTokenRepository.save(token);

        return token.getToken();
    }

    // ----------------- REFRESH ACCESS TOKEN ------------------
    public String refreshAccessToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        return jwtUtil.generateToken(token.getUser().getUsername());
    }
}
