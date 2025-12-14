package com.tasksync.dto;

import com.tasksync.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class WsNotificationDTO {

    private NotificationType type;
    private String message;

    private Long projectId;
    private Long taskId;

    private Instant createdAt;
}
