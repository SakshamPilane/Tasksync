package com.tasksync.service;

import com.tasksync.dto.*;
import com.tasksync.entity.Project;
import com.tasksync.entity.ProjectActivity;
import com.tasksync.entity.User;
import com.tasksync.repository.ProjectActivityRepository;
import com.tasksync.repository.ProjectRepository;
import com.tasksync.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectActivityRepository projectActivityRepository;

    // ========================= CREATE PROJECT =========================
    public ProjectResponseDTO createProject(CreateProjectRequest request, String creatorUsername) {

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());

        User manager;
        if (request.getManagerId() != null) {
            manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));
        } else {
            manager = creator;
        }

        project.setManager(manager);
        project.getMembers().add(manager);

        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);

        logActivity(saved, creator, "Created the project");

        return mapToDTO(saved);
    }

    // ========================= GET PROJECT BY ID =========================
    public ProjectResponseDTO getProjectById(Long projectId, String username, String role) {

        Project project = authorizeProjectAccess(projectId, username, role);
        return mapToDTO(project);
    }

    // ========================= UPDATE PROJECT =========================
    public ProjectResponseDTO updateProject(
            Long projectId,
            UpdateProjectRequest request,
            String username,
            String role
    ) {

        Project project = authorizeProjectAccess(projectId, username, role);

        if (request.getName() != null) {
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        // ---- Manager update (Option B: must already be member)
        if (request.getManagerId() != null) {

            User newManager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            if (!project.getMembers().contains(newManager)) {
                throw new RuntimeException("Manager must be added as project member first.");
            }

            project.setManager(newManager);
        }

        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Updated project details");

        projectRepository.save(project);

        return mapToDTO(project);
    }

    // ========================= ADD MEMBER =========================
    public String addMember(Long projectId, Long userId, String username, String role) {

        Project project = authorizeProjectAccess(projectId, username, role);

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (project.getMembers().contains(userToAdd)) {
            return "User already a project member";
        }

        project.getMembers().add(userToAdd);
        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Added member: " + userToAdd.getUsername());

        projectRepository.save(project);

        return "User added to project";
    }

    // ========================= REMOVE MEMBER =========================
    public String removeMember(Long projectId, Long userId, String username, String role) {

        Project project = authorizeProjectAccess(projectId, username, role);

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (project.getManager() != null && project.getManager().equals(userToRemove)) {
            throw new RuntimeException("Cannot remove the current project manager from members.");
        }

        project.getMembers().remove(userToRemove);
        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Removed member: " + userToRemove.getUsername());

        projectRepository.save(project);

        return "User removed from project";
    }

    // ========================= ARCHIVE PROJECT =========================
    public String archiveProject(Long projectId, String username, String role) {

        Project project = authorizeProjectAccess(projectId, username, role);

        if (project.isArchived()) {
            return "Project already archived";
        }

        project.setArchived(true);
        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Archived the project");

        projectRepository.save(project);

        return "Project archived";
    }

    // ========================= PROJECT ACTIVITIES =========================
    public List<ProjectActivityDTO> getProjectActivities(
            Long projectId,
            String username,
            String role
    ) {

        Project project = authorizeProjectAccess(projectId, username, role);

        return projectActivityRepository
                .findByProjectOrderByCreatedAtDesc(project)
                .stream()
                .map(a -> new ProjectActivityDTO(
                        a.getActor().getUsername(),
                        a.getAction(),
                        a.getCreatedAt()
                ))
                .toList();
    }

    // ========================= PAGINATED LIST =========================
    public Page<ProjectResponseDTO> getProjectsPaged(
            String username,
            String role,
            int page,
            int size,
            String search,
            String sortBy,
            String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // ADMIN → all projects
        if (role.equals("ROLE_ADMIN")) {
            return projectRepository
                    .findByDeletedFalseAndNameContainingIgnoreCase(search, pageable)
                    .map(this::mapToDTO);
        }

        // MANAGER → only member projects
        if (role.equals("ROLE_MANAGER")) {
            User manager = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return projectRepository
                    .findByDeletedFalseAndMembersContainingAndNameContainingIgnoreCase(
                            manager, search, pageable
                    )
                    .map(this::mapToDTO);
        }

        throw new RuntimeException("Access denied");
    }

    // ========================= AUTHORIZATION =========================
    private Project authorizeProjectAccess(Long projectId, String username, String role) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (role.equals("ROLE_ADMIN")) {
            return project;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (project.getMembers().contains(user)) {
            return project;
        }

        throw new RuntimeException("Access denied to this project");
    }

    // ========================= ACTIVITY LOGGER =========================
    private void logActivity(Project project, User actor, String action) {

        ProjectActivity activity = new ProjectActivity();
        activity.setProject(project);
        activity.setActor(actor);
        activity.setAction(action);

        projectActivityRepository.save(activity);
    }

    // ========================= DTO MAPPER =========================
    private ProjectResponseDTO mapToDTO(Project project) {

        List<ProjectMemberDTO> members = project.getMembers()
                .stream()
                .map(u -> new ProjectMemberDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRoles()
                                .stream()
                                .map(r -> r.getName())
                                .collect(Collectors.toSet())
                ))
                .toList();

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setManager(project.getManager() != null ? project.getManager().getUsername() : null);
        dto.setMembers(members);
        dto.setArchived(project.isArchived());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        return dto;
    }

    // ========================= SOFT DELETE (TRASH) =========================
    public String deleteProject(Long projectId, String username, String role) {

        Project project = authorizeProjectAccess(projectId, username, role);

        if (!role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only admin can delete projects");
        }

        if (project.isDeleted()) {
            return "Project already in trash";
        }

        project.setDeleted(true);
        project.setArchived(false);
        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Moved project to trash");

        projectRepository.save(project);

        return "Project moved to trash";
    }

    // ========================= RESTORE PROJECT =========================
    public String restoreProject(Long projectId, String username, String role) {

        if (!role.equals("ROLE_ADMIN")) {
            throw new RuntimeException("Only admin can restore projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.isDeleted()) {
            return "Project is not in trash";
        }

        project.setDeleted(false);
        project.setUpdatedAt(Instant.now());

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Actor not found"));

        logActivity(project, actor, "Restored project from trash");

        projectRepository.save(project);

        return "Project restored";
    }

    // ========================= LIST TRASH =========================
    public Page<ProjectResponseDTO> getTrashedProjects(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return projectRepository
                .findByDeletedTrue(pageable)
                .map(this::mapToDTO);
    }
}