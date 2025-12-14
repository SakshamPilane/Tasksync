package com.tasksync.controller;

import com.tasksync.dto.*;
import com.tasksync.entity.TaskStatus;
import com.tasksync.service.TaskService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // ========================= CREATE =========================
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public TaskResponseDTO createTask(
            @RequestBody CreateTaskRequest request,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.createTask(request, auth.getName(), role);
    }

    // ========================= UPDATE =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public TaskResponseDTO updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.updateTask(id, request, auth.getName(), role);
    }

    // ========================= ASSIGN =========================
    @PutMapping("/{id}/assign/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public TaskResponseDTO assignTask(
            @PathVariable Long id,
            @PathVariable Long userId,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.assignTask(id, userId, auth.getName(), role);
    }

    // ========================= CHANGE STATUS =========================
    @PutMapping("/{id}/status")
    public TaskResponseDTO changeStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.changeStatus(id, status, auth.getName(), role);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(
            @PathVariable Long id,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.getTaskById(id, auth.getName(), role);
    }

    // ========================= LIST BY PROJECT =========================
    @GetMapping("/project/{projectId}")
    public Page<TaskResponseDTO> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {

        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.getTasksByProject(
                projectId,
                PageRequest.of(page, size),
                auth.getName(),
                role
        );
    }

    // ========================= TASK ACTIVITY FEED =========================
    @GetMapping("/{id}/activities")
    public List<TaskActivityDTO> getTaskActivities(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return taskService.getTaskActivities(id, auth.getName(), role);
    }

}
