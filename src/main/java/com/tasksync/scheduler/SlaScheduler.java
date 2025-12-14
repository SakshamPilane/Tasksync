package com.tasksync.scheduler;

import com.tasksync.entity.NotificationType;
import com.tasksync.entity.Task;
import com.tasksync.entity.TaskActivity;
import com.tasksync.entity.TaskStatus;
import com.tasksync.repository.TaskActivityRepository;
import com.tasksync.repository.TaskRepository;

import com.tasksync.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaScheduler {

    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final NotificationService notificationService;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void checkSlaBreaches() {

        List<Task> tasks = taskRepository
                .findBySlaHoursNotNullAndSlaBreachedFalseAndStatusNot(TaskStatus.DONE);

        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {

            if (task.getSlaDeadline() != null && now.isAfter(task.getSlaDeadline())) {

                // ---- Mark breached ----
                task.setSlaBreached(true);
                task.setUpdatedAt(Instant.now());
                taskRepository.save(task);

                // ---- Log breach ----
                TaskActivity breachActivity = new TaskActivity();
                breachActivity.setTask(task);
                breachActivity.setActor(task.getProject().getManager()); // ✅ FIX
                breachActivity.setAction("SLA breached for task");

                taskActivityRepository.save(breachActivity);

                notificationService.createNotification(
                        task.getProject().getManager(),
                        NotificationType.SLA_BREACHED,
                        "SLA breached for task: " + task.getTitle(),
                        task.getProject().getId(),
                        task.getId()
                );

                // ---- Escalate (safe) ----
                if (!task.isEscalated()) {
                    escalate(task);
                }

                log.warn("SLA breached for Task ID {}", task.getId());
            }
        }
    }

    // ---------------- ESCALATION ----------------
    private void escalate(Task task) {

        if (task.isEscalated()) {
            return; // idempotent
        }

        task.setEscalated(true);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);

        TaskActivity escalationActivity = new TaskActivity();
        escalationActivity.setTask(task);
        escalationActivity.setActor(task.getProject().getManager());
        escalationActivity.setAction(
                "SLA escalated to project manager: "
                        + task.getProject().getManager().getUsername()
        );

        taskActivityRepository.save(escalationActivity);

        notificationService.createNotification(
                task.getProject().getManager(),
                NotificationType.SLA_ESCALATED,
                "SLA escalated for task: " + task.getTitle(),
                task.getProject().getId(),
                task.getId()
        );
    }
}
