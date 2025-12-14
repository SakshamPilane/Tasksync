package com.tasksync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    // ---------------- Project Manager ----------------
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // ---------------- Project Members ----------------
    @ManyToMany
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // ---------------- Project State ----------------
    private boolean archived = false;   // inactive but visible
    private boolean deleted = false;    // soft deleted (trash)

    // ---------------- Progress (Task Module Hook) ----------------
    private int totalTasks = 0;
    private int completedTasks = 0;
    private double progressPercentage = 0.0;

    // ---------------- Timestamps ----------------
    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}