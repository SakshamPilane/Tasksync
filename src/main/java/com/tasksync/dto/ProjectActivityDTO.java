package com.tasksync.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class ProjectActivityDTO {

    private String actor;      // username
    private String action;     // description
    private Instant createdAt;
}
