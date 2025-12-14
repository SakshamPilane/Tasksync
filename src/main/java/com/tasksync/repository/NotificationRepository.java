package com.tasksync.repository;

import com.tasksync.entity.Notification;
import com.tasksync.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // -------- Fetch notifications for a user (latest first) --------
    Page<Notification> findByRecipientOrderByCreatedAtDesc(
            User recipient,
            Pageable pageable
    );

    // -------- Fetch unread notifications --------
    List<Notification> findByRecipientAndReadFalse(User recipient);

    // -------- Count unread (badge count) --------
    long countByRecipientAndReadFalse(User recipient);
}
