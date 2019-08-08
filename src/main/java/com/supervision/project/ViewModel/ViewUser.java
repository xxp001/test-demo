package com.supervision.project.ViewModel;

import com.supervision.user.model.Department;
import lombok.Data;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-03-28 18:45
 *
 **/
@Data
public class ViewUser {

    private Integer id;

    private Integer roleId;

    private Department department;

    private Integer departmentId;

    private String departmentName;

    private String workCode;

    private String password;

    private String mobile;

    private Short userState;

    private String userRealname;

}
