package com.supervision.project.ViewModel;

import com.supervision.project.model.ProjectCost;
import com.supervision.user.model.Department;
import com.supervision.user.model.User;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-02-18 00:14
 *
 **/
@Data
public class ViewProject {

    private Integer id;

    private String projectName;

    private String projectCode;

    private String client;

    private String contractor;

    private Department department;

    private String partner;

    private User projectDirector;

    private User delegate;

    private Date createTime;

    private User createFrom;

    private String projectDesc;

    private Short projectState;  //0表示未开始，1表示正在进行，2表示已完成，3表示已关闭

    private Short projectType;

    private Date expectFinishTime;

    private Date finishTime;

    private Date startTime;

    private Integer process;

    private ProjectCost projectCost;

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
