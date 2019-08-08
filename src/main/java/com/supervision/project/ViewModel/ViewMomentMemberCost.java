package com.supervision.project.ViewModel;

import com.supervision.project.model.ExtraCost;
import com.supervision.project.model.HumanCostRecord;
import com.supervision.project.model.Moment;
import com.supervision.project.model.Project;
import com.supervision.user.model.User;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-03-26 00:05
 *
 **/
@Data
public class ViewMomentMemberCost {

    private User member;

    private String role;

    private Moment moment;

    private List<HumanCostRecord> humanCostRecords;

    private List<ExtraCost> extraCosts;

    private BigDecimal costHalfDay;

    private BigDecimal costInMoment;

}
