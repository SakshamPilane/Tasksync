package com.tasksync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "project_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which project this activity belongs to
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    // Who performed the action
    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id")
    private User actor;

    // Action description (human-readable)
    @Column(nullable = false)
    private String action;

    // Timestamp
    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
