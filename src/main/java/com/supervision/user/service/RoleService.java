package com.supervision.user.service;

import com.supervision.user.model.Role;

public interface RoleService {
    String assignRole(String workCode, Integer roleId);

    String addRole(String roleDesc);

    String delRole(Integer id);

    String modifyRole(Role role);

    String getAllRole();

    Role getRoleById(Integer roleId);

    String getRoleList();
}
