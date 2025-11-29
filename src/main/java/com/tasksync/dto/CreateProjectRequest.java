package com.tasksync.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProjectRequest {
    private String name;
    private String description;
    private Long managerId; // Optional
}
