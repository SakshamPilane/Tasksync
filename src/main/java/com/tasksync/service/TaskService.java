package com.tasksync.service;

import com.tasksync.dto.*;
import com.tasksync.entity.*;
import com.tasksync.repository.ProjectRepository;
import com.tasksync.repository.TaskActivityRepository;
import com.tasksync.repository.TaskRepository;
import com.tasksync.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final NotificationService notificationService;

    // ========================= CREATE TASK =========================
    public TaskResponseDTO createTask(
            CreateTaskRequest request,
            String username,
            String role
    ) {

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new RuntimeException("Only Admin or Manager can create tasks");
        }

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!role.equals("ROLE_ADMIN") && !project.getMembers().contains(creator)) {
            throw new RuntimeException("Only project members can create tasks");
        }

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setCreatedBy(creator);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());
        task.setSlaHours(request.getSlaHours());
        initializeSla(task);

        taskRepository.save(task);

        // ---- Activity + Progress ----
        logTaskActivity(task, creator, "Created the task");
        recalculateProjectProgress(project);
        projectRepository.save(project);

        return mapToDTO(task);
    }

    // ========================= UPDATE TASK =========================
    public TaskResponseDTO updateTask(
            Long taskId,
            UpdateTaskRequest request,
            String username,
            String role
    ) {

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new RuntimeException("Only Admin or Manager can update tasks");
        }

        Task task = getAuthorizedTask(taskId, username, role);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);

        User actor = userRepository.findByUsername(username).orElseThrow();
        logTaskActivity(task, actor, "Updated task details");

        return mapToDTO(task);
    }

    // ========================= ASSIGN TASK =========================
    public TaskResponseDTO assignTask(
            Long taskId,
            Long assigneeId,
            String username,
            String role
    ) {

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_MANAGER")) {
            throw new RuntimeException("Only Admin or Manager can assign tasks");
        }

        Task task = getAuthorizedTask(taskId, username, role);

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee not found"));

        if (!task.getProject().getMembers().contains(assignee)) {
            throw new RuntimeException("Assignee must be a project member");
        }

        task.setAssignee(assignee);
        resetSla(task);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);

        notificationService.createNotification(
                assignee,
                NotificationType.TASK_ASSIGNED,
                "You have been assigned a task: " + task.getTitle(),
                task.getProject().getId(),
                task.getId()
        );

        User actor = userRepository.findByUsername(username).orElseThrow();
        logTaskActivity(task, actor, "Assigned task to " + assignee.getUsername());

        return mapToDTO(task);
    }

    // ========================= CHANGE STATUS =========================
    public TaskResponseDTO changeStatus(
            Long taskId,
            TaskStatus status,
            String username,
            String role
    ) {

        Task task = getAuthorizedTask(taskId, username, role);
        TaskStatus oldStatus = task.getStatus();

        // ---- SLA Logic ----
        if (status == TaskStatus.DONE) {
            clearSla(task);
        }

        // Reopen task → reset SLA
        if (oldStatus == TaskStatus.DONE && status != TaskStatus.DONE) {
            resetSla(task);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!role.equals("ROLE_ADMIN")
                && !role.equals("ROLE_MANAGER")
                && (task.getAssignee() == null || !task.getAssignee().equals(user))) {
            throw new RuntimeException("Only assignee can change task status");
        }

        task.setStatus(status);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);

        if (!task.getCreatedBy().equals(user)) {
            notificationService.createNotification(
                    task.getCreatedBy(),
                    NotificationType.TASK_STATUS_CHANGED,
                    "Task '" + task.getTitle() + "' status changed to " + status,
                    task.getProject().getId(),
                    task.getId()
            );
        }

        logTaskActivity(
                task,
                user,
                "Changed status from " + oldStatus + " to " + status
        );

        // ---- Recalculate project progress ONLY if DONE affected ----
        if ((oldStatus != TaskStatus.DONE && status == TaskStatus.DONE)
                || (oldStatus == TaskStatus.DONE && status != TaskStatus.DONE)) {

            Project project = task.getProject();
            recalculateProjectProgress(project);
            projectRepository.save(project);
        }

        return mapToDTO(task);
    }

    // ========================= GET TASK BY ID =========================
    public TaskResponseDTO getTaskById(
            Long taskId,
            String username,
            String role
    ) {

        Task task = getAuthorizedTask(taskId, username, role);
        return mapToDTO(task);
    }

    // ========================= LIST TASKS BY PROJECT =========================
    public Page<TaskResponseDTO> getTasksByProject(
            Long projectId,
            Pageable pageable,
            String username,
            String role
    ) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!role.equals("ROLE_ADMIN") && !project.getMembers().contains(user)) {
            throw new RuntimeException("Access denied to project tasks");
        }

        return taskRepository.findByProject(project, pageable)
                .map(this::mapToDTO);
    }

    // ========================= AUTHORIZATION =========================
    private Task getAuthorizedTask(Long taskId, String username, String role) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (role.equals("ROLE_ADMIN")) {
            return task;
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (task.getProject().getMembers().contains(user)) {
            return task;
        }

        throw new RuntimeException("Access denied to task");
    }

    // ========================= ACTIVITY LOGGER =========================
    private void logTaskActivity(Task task, User actor, String action) {

        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setActor(actor);
        activity.setAction(action);

        taskActivityRepository.save(activity);
    }

    // ========================= PROJECT PROGRESS =========================
    private void recalculateProjectProgress(Project project) {

        long total = taskRepository.countByProject(project);
        long completed = taskRepository.countByProjectAndStatus(project, TaskStatus.DONE);

        project.setTotalTasks((int) total);
        project.setCompletedTasks((int) completed);

        double progress = total == 0 ? 0.0 : (completed * 100.0) / total;
        project.setProgressPercentage(progress);

        project.setUpdatedAt(Instant.now());
    }

    // ========================= DTO MAPPER =========================
    private TaskResponseDTO mapToDTO(Task task) {

        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setProjectId(task.getProject().getId());
        dto.setAssignee(task.getAssignee() != null ? task.getAssignee().getUsername() : null);
        dto.setCreatedBy(task.getCreatedBy().getUsername());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }

    // ========================= SLA HELPERS =========================
    private void initializeSla(Task task) {

        if (task.getSlaHours() == null) {
            return;
        }

        task.setSlaDeadline(
                task.getCreatedAt()
                        .plusSeconds(task.getSlaHours() * 3600L)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        task.setSlaBreached(false);
    }

    private void resetSla(Task task) {

        if (task.getSlaHours() == null) {
            return;
        }

        task.setSlaDeadline(
                Instant.now()
                        .plusSeconds(task.getSlaHours() * 3600L)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        task.setSlaBreached(false);
    }

    private void clearSla(Task task) {
        task.setSlaBreached(false);
    }

    // ========================= TASK ACTIVITY FEED =========================
    public List<TaskActivityDTO> getTaskActivities(
            Long taskId,
            String username,
            String role
    ) {

        Task task = getAuthorizedTask(taskId, username, role);

        return taskActivityRepository
                .findByTaskOrderByCreatedAtDesc(task)
                .stream()
                .map(a -> new TaskActivityDTO(
                        a.getActor().getUsername(),
                        a.getAction(),
                        a.getCreatedAt()
                ))
                .toList();
    }
}