package com.tasksync.controller;

import com.tasksync.dto.CreateProjectRequest;
import com.tasksync.dto.ProjectActivityDTO;
import com.tasksync.dto.ProjectResponseDTO;
import com.tasksync.dto.UpdateProjectRequest;
import com.tasksync.service.ProjectService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ========================= CREATE =========================
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ProjectResponseDTO createProject(
            @RequestBody CreateProjectRequest request,
            Authentication auth
    ) {
        return projectService.createProject(request, auth.getName());
    }

    // ========================= UPDATE =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public ProjectResponseDTO updateProject(
            @PathVariable Long id,
            @RequestBody UpdateProjectRequest request,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.updateProject(id, request, auth.getName(), role);
    }

    // ========================= GET BY ID =========================
    @GetMapping("/{id}")
    public ProjectResponseDTO getProjectById(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.getProjectById(id, auth.getName(), role);
    }

    // ========================= LIST (PAGINATED) =========================
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public Page<ProjectResponseDTO> getProjects(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();

        return projectService.getProjectsPaged(
                auth.getName(),
                role,
                page,
                size,
                search,
                sortBy,
                sortDir
        );
    }

    // ========================= ADD MEMBER =========================
    @PutMapping("/{projectId}/add-member/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public String addMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.addMember(projectId, userId, auth.getName(), role);
    }

    // ========================= REMOVE MEMBER =========================
    @PutMapping("/{projectId}/remove-member/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public String removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.removeMember(projectId, userId, auth.getName(), role);
    }

    // ========================= ARCHIVE =========================
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String archiveProject(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.archiveProject(id, auth.getName(), role);
    }

    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public String unarchiveProject(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.unarchiveProject(id, auth.getName(), role);
    }

    // ========================= ACTIVITIES =========================
    @GetMapping("/{id}/activities")
    public List<ProjectActivityDTO> getProjectActivities(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.getProjectActivities(id, auth.getName(), role);
    }

    // ========================= TRASH =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteProject(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.deleteProject(id, auth.getName(), role);
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String restoreProject(
            @PathVariable Long id,
            Authentication auth
    ) {
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return projectService.restoreProject(id, auth.getName(), role);
    }

    @GetMapping("/trash")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<ProjectResponseDTO> getTrash(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return projectService.getTrashedProjects(page, size, sortBy, sortDir);
    }
}
