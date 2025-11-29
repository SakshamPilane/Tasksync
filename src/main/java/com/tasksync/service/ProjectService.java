package com.tasksync.service;

import com.tasksync.dto.CreateProjectRequest;
import com.tasksync.dto.ProjectMemberDTO;
import com.tasksync.dto.ProjectResponseDTO;
import com.tasksync.dto.UpdateProjectRequest;
import com.tasksync.entity.Project;
import com.tasksync.entity.User;
import com.tasksync.repository.ProjectRepository;
import com.tasksync.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ---------------------- CREATE PROJECT ----------------------
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

        // Manager MUST be a member
        project.getMembers().add(manager);

        project.setManager(manager);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());

        Project saved = projectRepository.save(project);

        return mapToDTO(saved);
    }

    // ---------------------- UPDATE PROJECT ----------------------
    public ProjectResponseDTO updateProject(Long id, UpdateProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (request.getName() != null) project.setName(request.getName());
        if (request.getDescription() != null) project.setDescription(request.getDescription());

        // ----- Manager Update (Option B Logic) -----
        if (request.getManagerId() != null) {

            User newManager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            // Check if manager is member
            if (!project.getMembers().contains(newManager)) {
                throw new RuntimeException("Manager must be added as project member first.");
            }

            project.setManager(newManager);
        }

        project.setUpdatedAt(Instant.now());

        projectRepository.save(project);

        return mapToDTO(project);
    }

    // ---------------------- GET PROJECT BY ID ----------------------
    public ProjectResponseDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return mapToDTO(project);
    }

    // ---------------------- GET ALL PROJECTS ----------------------
    public List<ProjectResponseDTO> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    // ---------------------- ARCHIVE PROJECT ----------------------
    public String archiveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        project.setArchived(true);
        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);

        return "Project archived";
    }

    // ---------------------- ADD MEMBER ----------------------
    public String addMember(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        project.getMembers().add(user);
        project.setUpdatedAt(Instant.now());

        projectRepository.save(project);

        return "User added to project";
    }

    // ---------------------- REMOVE MEMBER ----------------------
    public String removeMember(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow();

        User user = userRepository.findById(userId)
                .orElseThrow();

        project.getMembers().remove(user);

        // If removed user is manager → throw error
        if (project.getManager() != null && project.getManager().equals(user)) {
            throw new RuntimeException("Cannot remove the current project manager from members.");
        }

        project.setUpdatedAt(Instant.now());
        projectRepository.save(project);

        return "User removed from project";
    }

    // ---------------------- DTO MAPPER ----------------------
    private ProjectResponseDTO mapToDTO(Project project) {

        List<ProjectMemberDTO> memberDTOs = project.getMembers()
                .stream()
                .map(u -> new ProjectMemberDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet())
                ))
                .collect(Collectors.toList());

        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());

        dto.setManager(project.getManager() != null ? project.getManager().getUsername() : null);

        dto.setMembers(memberDTOs);
        dto.setArchived(project.isArchived());

        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        return dto;
    }
}
