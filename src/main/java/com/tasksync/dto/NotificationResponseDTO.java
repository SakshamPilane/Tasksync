package com.tasksync.dto;

import com.tasksync.entity.NotificationType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationResponseDTO {

    private Long id;
    private NotificationType type;
    private String message;

    private Long projectId;
    private Long taskId;

    private boolean read;
    private Instant createdAt;
}
