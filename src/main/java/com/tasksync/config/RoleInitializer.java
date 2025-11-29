package com.tasksync.config;

import com.tasksync.entity.Role;
import com.tasksync.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("ROLE_ADMIN");
        createRoleIfNotExists("ROLE_MANAGER");
        createRoleIfNotExists("ROLE_USER");

        System.out.println("🎉 Roles initialized successfully");
    }

    private void createRoleIfNotExists(String roleName) {
        roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
    }
}
