package com.tasksync.controller;

import com.tasksync.dto.NotificationResponseDTO;
import com.tasksync.service.NotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ========================= FETCH MY NOTIFICATIONS =========================
    @GetMapping
    public Page<NotificationResponseDTO> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth
    ) {

        return notificationService.getMyNotifications(
                auth.getName(),
                PageRequest.of(page, size)
        );
    }

    // ========================= UNREAD COUNT =========================
    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication auth) {
        return notificationService.getUnreadCount(auth.getName());
    }

    // ========================= MARK ONE AS READ =========================
    @PutMapping("/{id}/read")
    public void markAsRead(
            @PathVariable Long id,
            Authentication auth
    ) {
        notificationService.markAsRead(id, auth.getName());
    }

    // ========================= MARK ALL AS READ =========================
    @PutMapping("/read-all")
    public void markAllAsRead(Authentication auth) {
        notificationService.markAllAsRead(auth.getName());
    }
}
