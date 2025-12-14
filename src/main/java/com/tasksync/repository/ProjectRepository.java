package com.tasksync.repository;

import com.tasksync.entity.Project;
import com.tasksync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Admin: search all projects
    Page<Project> findByDeletedFalseAndNameContainingIgnoreCase(
            String name, Pageable pageable
    );

    // Manager: search only member projects
    Page<Project> findByDeletedFalseAndMembersContainingAndNameContainingIgnoreCase(
            User member, String name, Pageable pageable
    );

    Page<Project> findByDeletedTrue(Pageable pageable);
}
