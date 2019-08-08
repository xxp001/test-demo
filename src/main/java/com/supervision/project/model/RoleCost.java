package com.supervision.project.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoleCost {
    private Integer roleId;

    private BigDecimal roleCostHalfDay;

    private String roleCostDesc;
}