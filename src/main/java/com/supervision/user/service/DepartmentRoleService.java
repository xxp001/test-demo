package com.supervision.user.service;

import java.util.List;

public interface DepartmentRoleService {
    String delDeptOfRole(Integer deptId);

    String modifyDeptOfRole(Integer deptId, List<Integer> roleIds);

    String getOrganizationInfo();
}
