package com.supervision.user.service;

import com.supervision.user.model.Department;
import com.supervision.user.model.User;

/**
 * @ClassName DepartmentService
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/15 15:01
 **/
public interface DepartmentService {
    String addDepartment(Department department);

    Department getDepartment(Integer departmentId);

    String getDepartmentByCode(User user);

    String getAllDepartment();

    String delDept(Integer deptId);

    String getDeptOfUser();

    String addDept(Department department);

    String modifyDept(Department department);

    String getOrganization();

    String getAllDeptInfo();
}
