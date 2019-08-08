package com.supervision.user.mapper;

import com.supervision.user.model.Permission;

import java.util.List;

public interface PermissionMapper {
    int insert(Permission record);

    int insertSelective(Permission record);

    int getPermissionCount(Permission permission);

    List<Permission> getPermissionId(String permission);

    void modifyPermission(Permission permission);

    String getIdOfPermission(String permission);

    List<Permission> getPermissionById(Integer permissionId);

    Permission selectPermissionById(int id);

    List<Permission> getPermissionList();

    List<Permission> getPermissionByRoleId(Integer roleId);
}