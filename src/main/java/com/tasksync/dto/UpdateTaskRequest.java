package com.tasksync.dto;

import com.tasksync.entity.TaskPriority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateTaskRequest {

    private String title;
    private String description;
    private TaskPriority priority;
    private LocalDate dueDate;
}
