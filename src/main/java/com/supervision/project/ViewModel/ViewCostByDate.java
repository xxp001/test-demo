package com.supervision.project.ViewModel;

import com.supervision.project.model.ExtraCost;
import com.supervision.project.model.HumanCostRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-04-12 09:50
 *
 **/
@Data
public class ViewCostByDate {

    private Integer projectId;

    private Integer momentId;

    private Integer uploadFrom;

    private String uploaderName;

    private String workCode;

    private Integer roleInProject;

    private String role;

    private String uploadedTime;

    private Date uploadedTimeObject;

    private Integer workTime;

    private BigDecimal costHalfDay;

    private BigDecimal humanCostAmount;

    private LinkedList<ExtraCost> extraCosts;

    private HumanCostRecord humanCostRecord;

}
