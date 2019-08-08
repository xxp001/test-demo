package com.supervision.project.model;

import com.supervision.document.model.Document;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:Custom Project And Moments
 * @Author:TjSanshao
 * @Create:2019-02-16 18:32
 *
 **/
@Data
public class CustomProject {

    private String projectName;

    private String projectCode;

    private String client;

    private String contractor;

    private String partner;

    private Integer projectDirector;

    private Integer delegate;

    private Integer createFrom;

    private Integer department;

    private Short projectType;

    private String projectDesc;

    private Date expectFinishTime;

    String moments;

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
