package com.supervision.census.model;

import com.supervision.project.model.Project;
import lombok.Data;

import java.math.BigDecimal;

/*
 * @Project:SupervisionSystem
 * @Description:model
 * @Author:TjSanshao
 * @Create:2019-04-13 22:21
 *
 **/
@Data
public class CensusProjectCost {

    private Project project;

    // 人力
    private BigDecimal humanCost;

    // 差旅
    private BigDecimal stay;

    // 伙食
    private BigDecimal meals;

    // 交通
    private BigDecimal traffic;

    // 其他
    private BigDecimal others;

}
