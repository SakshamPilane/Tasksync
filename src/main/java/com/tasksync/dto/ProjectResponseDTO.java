package com.tasksync.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ProjectResponseDTO {

    private Long id;
    private String name;
    private String description;

    private String manager;

    private boolean archived;

    private List<ProjectMemberDTO> members;

    private Instant createdAt;
    private Instant updatedAt;
}
