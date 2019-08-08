package com.supervision.project.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MomentCost {
    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private BigDecimal expectCost;

    private BigDecimal realCost;
}