package com.supervision.user.mapper;

import com.supervision.user.model.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface RoleMapper {
    int insert(Role record);

    int insertSelective(Role record);

    Role getRoleInfo(Role role);

    int delRole(Integer id);

    void modifyRole(Role role);

    void setRole(@Param("workCode") String workCode, @Param("roleId") String roleId);

    int selectRole(String roleId);

    String getRoleIdByRoledesc(String roledesc);

    List<Role> getAllRole();

    Role selectRoleById(Integer id);

    List<Role> getRoleByDeptId(Integer deptId);
}