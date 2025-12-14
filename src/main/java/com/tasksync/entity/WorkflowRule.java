package com.tasksync.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "workflow_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // What event triggers this rule
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowEventType eventType;

    // Conditions (JSON stored as String)
    @Column(columnDefinition = "TEXT")
    private String conditionsJson;

    // Actions (JSON stored as String)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String actionsJson;

    // Can be turned on/off without deleting
    private boolean enabled = true;

    // Metadata
    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}
