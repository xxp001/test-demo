package com.supervision.project.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectCost {
    private Integer id;

    private Integer projectId;

    private BigDecimal expectCost;

    private BigDecimal realCost;
}