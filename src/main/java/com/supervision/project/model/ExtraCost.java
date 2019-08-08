package com.supervision.project.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ExtraCost {

    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private Integer uploadFrom;

    private Date uploadTime;

    private BigDecimal amount;

    private Short applyState;

    private String workCode;

    private String uploaderName;

    private String costDesc;

    private String costRemarks;
}