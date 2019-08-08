package com.supervision.user.service;

import com.supervision.user.model.Permission;


public interface PermissionService {
    String setPermission(Permission permission);

    String assignPermissions(Integer roleId, String[] permissionIds);

    String modifyPermission(Permission permission);

    String getPermissionList();
}
