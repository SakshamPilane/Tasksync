package com.tasksync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasksync.entity.*;
import com.tasksync.repository.TaskRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowActionExecutor {

    private final NotificationService notificationService;
    private final TaskRepository taskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========================= EXECUTE ACTIONS =========================
    public void execute(String actionsJson, Map<String, Object> context) {

        try {
            Map<String, Object> actions =
                    objectMapper.readValue(actionsJson, new TypeReference<>() {});

            // ---------- NOTIFY ----------
            if (actions.containsKey("notify")) {
                List<String> targets = (List<String>) actions.get("notify");
                notifyTargets(targets, context);
            }

            // ---------- SET PRIORITY ----------
            if (actions.containsKey("setPriority")) {
                setTaskPriority((String) actions.get("setPriority"), context);
            }

            // ---------- RESET SLA ----------
            if (Boolean.TRUE.equals(actions.get("resetSla"))) {
                resetSla(context);
            }

            // ---------- ESCALATE ----------
            if (Boolean.TRUE.equals(actions.get("escalate"))) {
                escalate(context);
            }

        } catch (Exception e) {
            log.error("Action execution failed", e);
        }
    }

    // ========================= HELPERS =========================

    private void notifyTargets(List<String> targets, Map<String, Object> context) {

        Task task = (Task) context.get("task");

        for (String target : targets) {

            User recipient = switch (target) {
                case "ASSIGNEE" -> task.getAssignee();
                case "CREATOR" -> task.getCreatedBy();
                case "MANAGER" -> task.getProject().getManager();
                default -> null;
            };

            if (recipient != null) {
                notificationService.createNotification(
                        recipient,
                        NotificationType.TASK_STATUS_CHANGED,
                        "Workflow triggered on task: " + task.getTitle(),
                        task.getProject().getId(),
                        task.getId()
                );
            }
        }
    }

    private void setTaskPriority(String priority, Map<String, Object> context) {

        Task task = (Task) context.get("task");
        task.setPriority(TaskPriority.valueOf(priority));
        task.setUpdatedAt(java.time.Instant.now());

        taskRepository.save(task);

        log.info("Workflow set task {} priority to {}", task.getId(), priority);
    }

    private void resetSla(Map<String, Object> context) {

        Task task = (Task) context.get("task");

        if (task.getSlaHours() != null) {
            task.setSlaBreached(false);
            task.setEscalated(false);
            task.setSlaDeadline(
                    LocalDateTime.now().plusHours(task.getSlaHours())
            );

            taskRepository.save(task);

            log.info("Workflow reset SLA for task {}", task.getId());
        }
    }

    private void escalate(Map<String, Object> context) {

        Task task = (Task) context.get("task");

        if (!task.isEscalated()) {
            task.setEscalated(true);
            taskRepository.save(task);

            notificationService.createNotification(
                    task.getProject().getManager(),
                    NotificationType.SLA_ESCALATED,
                    "Workflow escalation for task: " + task.getTitle(),
                    task.getProject().getId(),
                    task.getId()
            );
        }
    }
}
