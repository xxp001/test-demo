package com.supervision.project.ViewModel;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-04-06 14:35
 *
 **/
@Data
public class ViewHumanCostRecord {

    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private Integer uploadFrom;

    private Date uploadTime;

    private Short workTime;

    private Short applyState;

    private String workCode;

    private String uploaderName;

    private BigDecimal costHalfDay;

    private BigDecimal humanCost;

}
