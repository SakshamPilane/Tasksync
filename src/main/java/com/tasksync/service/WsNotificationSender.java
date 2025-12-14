package com.tasksync.service;

import com.tasksync.dto.WsNotificationDTO;
import com.tasksync.entity.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WsNotificationSender {

    private final SimpMessagingTemplate messagingTemplate;

    // ========================= SEND TO USER =========================
    public void sendToUser(String username, WsNotificationDTO payload) {

        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                payload
        );
    }

    // ========================= SEND FROM NOTIFICATION ENTITY =========================
    public void sendFromNotification(Notification notification) {

        WsNotificationDTO payload = new WsNotificationDTO(
                notification.getType(),
                notification.getMessage(),
                notification.getProjectId(),
                notification.getTaskId(),
                notification.getCreatedAt()
        );

        sendToUser(notification.getRecipient().getUsername(), payload);
    }
}
