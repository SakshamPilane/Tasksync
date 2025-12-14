package com.tasksync.entity;

public enum WorkflowEventType {

    // -------- TASK EVENTS --------
    TASK_CREATED,
    TASK_UPDATED,
    TASK_ASSIGNED,
    TASK_STATUS_CHANGED,
    TASK_SLA_BREACHED,

    // -------- PROJECT EVENTS --------
    PROJECT_ARCHIVED
}
