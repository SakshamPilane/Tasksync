package com.tasksync.service;

import com.tasksync.dto.CreateUserRequest;
import com.tasksync.dto.UpdateUserRequest;
import com.tasksync.dto.UserResponseDTO;
import com.tasksync.entity.Role;
import com.tasksync.entity.User;
import com.tasksync.repository.RoleRepository;
import com.tasksync.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------- Fetch user profile ----------
    public UserResponseDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive()
        );
    }

    // ---------- Fetch all users (Admin/Manager) ----------
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getUsername(), u.getEmail(), u.isActive()))
                .toList();
    }

    // ---------- Change password ----------
    public String changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password updated successfully";
    }

    // ---------- Update user roles ----------
    public String updateUserRoles(Long userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        Role role = roleRepository.findByName(roleName)
                .orElseThrow();

        Set<Role> roles = new HashSet<>();
        roles.add(role);  // mutable set
        user.setRoles(roles);
        userRepository.save(user);

        return "User roles updated";
    }

    // ---------- Activate or deactivate user ----------
    public String setActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow();

        user.setActive(active);
        userRepository.save(user);

        return active ? "User activated" : "User deactivated";
    }

    // ---------- Create user (ADMIN) ----------
    public UserResponseDTO createUser(CreateUserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // ❗ FIX: Use mutable set instead of Set.of()
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        newUser.setRoles(roles);

        newUser.setActive(true);

        User saved = userRepository.save(newUser);

        return new UserResponseDTO(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.isActive()
        );
    }

    // ---------- Update profile (SELF) ----------
    public UserResponseDTO updateProfile(String username, UpdateUserRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }

        userRepository.save(user);

        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive()
        );
    }

}
