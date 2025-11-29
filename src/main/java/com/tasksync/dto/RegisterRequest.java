package com.tasksync.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role; // optional: ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
}
