package com.tasksync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- BASIC INFO ----------------
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    // ---------------- STATUS & PRIORITY ----------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM;

    // ---------------- DATES ----------------
    private LocalDate dueDate;

    // ---------------- SLA ----------------
    private Integer slaHours;
    private LocalDateTime slaDeadline;
    private boolean slaBreached = false;
    private boolean escalated = false;

    // ---------------- RELATIONSHIPS ----------------
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ---------------- TIMESTAMPS ----------------
    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}