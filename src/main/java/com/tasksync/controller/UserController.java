package com.tasksync.controller;

import com.tasksync.dto.CreateUserRequest;
import com.tasksync.dto.UpdateUserRequest;
import com.tasksync.dto.UserResponseDTO;
import com.tasksync.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------- GET PROFILE ----------
    @GetMapping("/me")
    public UserResponseDTO getMyProfile(Authentication auth) {
        return userService.getUserProfile(auth.getName());
    }

    // ---------- GET ALL USERS ----------
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    // ---------- CHANGE PASSWORD ----------
    @PutMapping("/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String newPassword) {
        return userService.changePassword(auth.getName(), newPassword);
    }

    // ---------- UPDATE USER ROLES (ADMIN ONLY) ----------
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String updateUserRoles(@PathVariable Long id,
                                  @RequestParam String role) {
        return userService.updateUserRoles(id, role);
    }

    // ---------- ACTIVATE / DEACTIVATE USER (ADMIN ONLY) ----------
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String setActiveStatus(@PathVariable Long id,
                                  @RequestParam boolean active) {
        return userService.setActiveStatus(id, active);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserResponseDTO createUser(@RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/update")
    public UserResponseDTO updateProfile(Authentication auth,
                                         @RequestBody UpdateUserRequest request) {
        return userService.updateProfile(auth.getName(), request);
    }
}
