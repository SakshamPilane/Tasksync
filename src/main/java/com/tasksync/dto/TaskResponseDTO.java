package com.tasksync.dto;

import com.tasksync.entity.TaskPriority;
import com.tasksync.entity.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class TaskResponseDTO {

    private Long id;
    private String title;
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    private LocalDate dueDate;

    private Long projectId;
    private String assignee;   // username
    private String createdBy;  // username

    private Instant createdAt;
    private Instant updatedAt;
}
