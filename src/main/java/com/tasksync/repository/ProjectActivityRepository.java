package com.tasksync.repository;

import com.tasksync.entity.Project;
import com.tasksync.entity.ProjectActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, Long> {

    List<ProjectActivity> findByProjectOrderByCreatedAtDesc(Project project);
}
