package com.supervision.project.model;

import lombok.Data;

@Data
public class ProjectMilestone {
    private Integer id;

    private String milestoneDocName;

    private String milestoneDocType;

    private Integer milestoneProcess;

    private Integer milestoneBonus;
}