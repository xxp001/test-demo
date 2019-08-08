package com.supervision.project.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class Project {

    // 主键Id
    private Integer id;

    // 项目名称
    private String projectName;

    // 项目代号
    private String projectCode;

    // 委托方
    private String client;

    // 承建方
    private String contractor;

    // 部门Id
    private Integer department;

    // 合作伙伴
    private String partner;

    // 项目总监
    private Integer projectDirector;

    // 总监代表
    private Integer delegate;

    // 项目立项时间
    private Date createTime;

    // 项目创建人Id
    private Integer createFrom;

    // 项目简介
    private String projectDesc;

    // 项目状态
    private Short projectState;  //0表示未开始，1表示正在进行，2表示终验，3表示已完成

    // 项目类型
    private Short projectType;

    // 预期完成时间
    private Date expectFinishTime;

    // 完成时间
    private Date finishTime;

    // 开始时间
    private Date startTime;

    // 创建人名字
    private String buildName;

    // 委托方联系方式，建议格式：唐毫峰 13420120424
    private String clientContact;

    // 我方经办人（应该是指签合同的人）
    private String personCharge;

    // 合同签订时间
    private Date agreementTime;

    // 建设工期，单位：月
    private Integer buildDuration;

    // 服务周期，单位：月
    private Integer serviceDuration;

    // 是否常驻，0不常驻，1常驻
    private Short isResident;

    // 工程预算
    private BigDecimal projectBudget;

    // 监理费
    private BigDecimal supervisionAmount;

    // 已付监理费
    private BigDecimal supervisionPaid;

    // 非常驻每周天数
    private Short notResidentWeekDays;
}