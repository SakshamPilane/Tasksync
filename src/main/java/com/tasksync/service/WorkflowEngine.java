package com.tasksync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasksync.entity.WorkflowEventType;
import com.tasksync.entity.WorkflowRule;
import com.tasksync.repository.WorkflowRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {

    private final WorkflowRuleRepository workflowRuleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WorkflowActionExecutor workflowActionExecutor;

    // ========================= EVENT ENTRY POINT =========================
    public void handleEvent(
            WorkflowEventType eventType,
            Map<String, Object> context
    ) {

        List<WorkflowRule> rules =
                workflowRuleRepository.findByEventTypeAndEnabledTrue(eventType);

        for (WorkflowRule rule : rules) {
            try {
                if (conditionsMatch(rule.getConditionsJson(), context)) {
                    executeActions(rule.getActionsJson(), context);
                }
            } catch (Exception e) {
                log.error("Workflow rule {} failed: {}", rule.getId(), e.getMessage());
            }
        }
    }

    // ========================= CONDITIONS =========================
    private boolean conditionsMatch(
            String conditionsJson,
            Map<String, Object> context
    ) {

        if (conditionsJson == null || conditionsJson.isBlank()) {
            return true; // No conditions = always match
        }

        try {
            Map<String, Object> conditions =
                    objectMapper.readValue(
                            conditionsJson,
                            new TypeReference<>() {}
                    );

            // Simple equals matching (v1)
            for (String key : conditions.keySet()) {
                if (!context.containsKey(key)) {
                    return false;
                }

                Object expected = conditions.get(key);
                Object actual = context.get(key);

                if (!expected.toString().equals(actual.toString())) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Condition parsing failed", e);
            return false;
        }
    }

    // ========================= ACTION DISPATCH =========================
    private void executeActions(
            String actionsJson,
            Map<String, Object> context
    ) {
        workflowActionExecutor.execute(actionsJson, context);
    }
}
