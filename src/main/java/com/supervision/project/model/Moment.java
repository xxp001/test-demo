package com.supervision.project.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "Moment")
public class Moment {
    private Integer id;

    private Integer projectId;

    @ApiModelProperty(value = "项目阶段在整个项目的序号，从1开始")
    private Integer momentOrder;

    @ApiModelProperty(value = "项目阶段介绍")
    private String momentDesc;

    @ApiModelProperty(value = "项目阶段状态，0表示未开始，1表示正在进行，2表示已完成")
    private Short state;

    @ApiModelProperty(value = "项目阶段的所需的文档总数")
    private Integer fileNumber;

    @ApiModelProperty(value = "项目阶段已上传的文档总数")
    private Integer uploadedFileNumber;

    @ApiModelProperty(value = "项目阶段预计完成时间")
    private Date expectFinishTime;

    @ApiModelProperty(value = "项目阶段完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "项目阶段开始时间")
    private Date startTime;

    @ApiModelProperty(value = "项目阶段名称")
    private String momentName;
}