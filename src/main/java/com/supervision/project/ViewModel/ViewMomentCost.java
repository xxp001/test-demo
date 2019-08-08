package com.supervision.project.ViewModel;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-02-25 12:09
 *
 **/
@Data
public class ViewMomentCost {
    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private BigDecimal expectCost;

    private BigDecimal realCost;

    private String momentName;
}
