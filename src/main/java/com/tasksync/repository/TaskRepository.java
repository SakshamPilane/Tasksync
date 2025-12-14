package com.tasksync.repository;

import com.tasksync.entity.Project;
import com.tasksync.entity.Task;
import com.tasksync.entity.User;
import com.tasksync.entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // -------- Tasks by Project --------
    Page<Task> findByProject(Project project, Pageable pageable);

    // -------- Tasks by Project + Status --------
    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);

    // -------- Tasks assigned to a user --------
    Page<Task> findByAssignee(User assignee, Pageable pageable);

    // -------- Count for progress calculation --------
    long countByProject(Project project);

    long countByProjectAndStatus(Project project, TaskStatus status);

    List<Task> findBySlaHoursNotNullAndSlaBreachedFalseAndStatusNot(TaskStatus status);
}
