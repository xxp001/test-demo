package com.supervision.project.ViewModel;

import lombok.Data;

import java.math.BigDecimal;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-04-13 12:19
 *
 **/
@Data
public class ViewProjectCostAmount {

    private Integer projectId;

    private Integer userId;

    private String realName;

    private String role;

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
