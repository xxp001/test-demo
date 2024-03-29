package com.supervision.user.mapper;

import com.supervision.user.model.Department;

import java.util.List;

public interface DepartmentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Department record);

    int insertSelective(Department record);

    Department selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Department record);

    int updateByPrimaryKey(Department record);

    List<Department> findAllDepartment();

    String getDepartmentIdByName(String dept);

    int isExistDept(String department);
}