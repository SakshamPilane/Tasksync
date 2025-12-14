package com.tasksync.repository;

import com.tasksync.entity.WorkflowEventType;
import com.tasksync.entity.WorkflowRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, Long> {

    List<WorkflowRule> findByEventTypeAndEnabledTrue(WorkflowEventType eventType);
}
