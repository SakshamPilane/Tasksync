package com.tasksync.controller;

import com.tasksync.dto.CreateProjectRequest;
import com.tasksync.dto.ProjectResponseDTO;
import com.tasksync.dto.UpdateProjectRequest;
import com.tasksync.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ProjectResponseDTO createProject(@RequestBody CreateProjectRequest request,
                                            Authentication auth) {
        return projectService.createProject(request, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ProjectResponseDTO updateProject(@PathVariable Long id,
                                            @RequestBody UpdateProjectRequest request) {
        return projectService.updateProject(id, request);
    }

    @GetMapping("/{id}")
    public ProjectResponseDTO getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public List<ProjectResponseDTO> getAll() {
        return projectService.getAllProjects();
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String archive(@PathVariable Long id) {
        return projectService.archiveProject(id);
    }

    @PutMapping("/{projectId}/add-member/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String addMember(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectService.addMember(projectId, userId);
    }

    @PutMapping("/{projectId}/remove-member/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        return projectService.removeMember(projectId, userId);
    }
}
