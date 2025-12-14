package com.tasksync.scheduler;

import com.tasksync.entity.Task;
import com.tasksync.entity.TaskActivity;
import com.tasksync.entity.TaskStatus;
import com.tasksync.entity.WorkflowEventType;
import com.tasksync.repository.TaskActivityRepository;
import com.tasksync.repository.TaskRepository;
import com.tasksync.service.WorkflowEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlaScheduler {

    private final TaskRepository taskRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final WorkflowEngine workflowEngine;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void checkSlaBreaches() {

        List<Task> tasks = taskRepository
                .findBySlaHoursNotNullAndSlaBreachedFalseAndStatusNot(TaskStatus.DONE);

        LocalDateTime now = LocalDateTime.now();

        for (Task task : tasks) {

            if (task.getSlaDeadline() != null && now.isAfter(task.getSlaDeadline())) {

                // ---------- Mark SLA breached ----------
                task.setSlaBreached(true);
                task.setUpdatedAt(Instant.now());
                taskRepository.save(task);

                // ---------- Log SLA breach activity ----------
                TaskActivity activity = new TaskActivity();
                activity.setTask(task);
                activity.setActor(task.getProject().getManager()); // system owner
                activity.setAction("SLA breached for task");

                taskActivityRepository.save(activity);

                // ---------- Emit workflow event ----------
                Map<String, Object> context = new HashMap<>();
                context.put("task", task);
                context.put("project", task.getProject());

                workflowEngine.handleEvent(
                        WorkflowEventType.TASK_SLA_BREACHED,
                        context
                );

                log.warn("SLA breached for Task ID {}", task.getId());
            }
        }
    }
}
