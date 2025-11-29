package com.tasksync.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String role; // ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
}
