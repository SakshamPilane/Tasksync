package com.tasksync.service;

import com.tasksync.dto.NotificationResponseDTO;
import com.tasksync.entity.Notification;
import com.tasksync.entity.NotificationType;
import com.tasksync.entity.User;
import com.tasksync.repository.NotificationRepository;
import com.tasksync.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WsNotificationSender wsNotificationSender;

    // ========================= CREATE (INTERNAL USE) =========================
    public void createNotification(
            User recipient,
            NotificationType type,
            String message,
            Long projectId,
            Long taskId
    ) {

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setProjectId(projectId);
        notification.setTaskId(taskId);

        Notification saved = notificationRepository.save(notification);

        // 🔔 Real-time push
        wsNotificationSender.sendFromNotification(saved);
    }

    // ========================= FETCH PAGINATED =========================
    public Page<NotificationResponseDTO> getMyNotifications(
            String username,
            Pageable pageable
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDTO);
    }

    // ========================= UNREAD COUNT =========================
    public long getUnreadCount(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByRecipientAndReadFalse(user);
    }

    // ========================= MARK ONE AS READ =========================
    public void markAsRead(Long notificationId, String username) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getUsername().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // ========================= MARK ALL AS READ =========================
    public void markAllAsRead(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationRepository
                .findByRecipientAndReadFalse(user)
                .forEach(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    // ========================= DTO MAPPER =========================
    private NotificationResponseDTO mapToDTO(Notification notification) {

        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setMessage(notification.getMessage());
        dto.setProjectId(notification.getProjectId());
        dto.setTaskId(notification.getTaskId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }
}
