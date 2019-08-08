package com.supervision.user.mapper;

import com.supervision.user.model.UserPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserPermissionMapper {
    int insert(UserPermission record);

    int insertSelective(UserPermission record);

    int getPermissionCountByRoleId(String roleId);

    List<UserPermission> getUserPermissionId(Integer roleId);

    int getRolePermissionInfo(@Param("roleId")Integer roleId,@Param("permissionId") Integer permissionId);

    int delRoleOfPermission(Integer roleId);


}