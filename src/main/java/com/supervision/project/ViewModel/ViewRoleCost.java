package com.supervision.project.ViewModel;

import com.supervision.user.model.Role;
import lombok.Data;

import java.math.BigDecimal;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-02-19 18:26
 *
 **/
@Data
public class ViewRoleCost {

    private Integer id;

    private BigDecimal roleCostHalfDay;

    private String roleCostDesc;

    // 角色
    private Role role;

    // 角色名称
    private String roleName;

}
