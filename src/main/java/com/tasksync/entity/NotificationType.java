package com.tasksync.entity;

public enum NotificationType {

    TASK_ASSIGNED,
    TASK_STATUS_CHANGED,
    SLA_BREACHED,
    SLA_ESCALATED,

    PROJECT_MEMBER_ADDED,
    PROJECT_MEMBER_REMOVED,
    PROJECT_ARCHIVED
}
