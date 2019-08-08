package com.supervision.user.mapper;

import com.supervision.user.model.DepartmentRole;

public interface DepartmentRoleMapper {
    int insert(DepartmentRole record);

    int insertSelective(DepartmentRole record);

    int deleteByPrimaryKey(Integer deptId);
}