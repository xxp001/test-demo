package com.supervision.project.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ProjectTeam")
public class ProjectTeam {

    @ApiModelProperty(value = "主键Id")
    private Integer id;

    @ApiModelProperty(value = "项目Id")
    private Integer projectId;

    @ApiModelProperty(value = "用户Id")
    private Integer userId;

    @ApiModelProperty(value = "在项目中的角色，0表示普通成员，1表示总监代表，2表示项目总监，此项不需填写")
    private Short roleInProject;
}