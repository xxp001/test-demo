package com.supervision.project.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class HumanCostRecord {

    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private Integer uploadFrom;

    private Date uploadTime;

    private Short workTime;

    private Short applyState;

    private String workCode;

    private String uploaderName;
}