package com.supervision.user.service;




import java.util.List;

public interface UserPermissionService {

    String getRoleOfPermission(Integer roleId);

    String modifyRoleOfPermission(Integer roleId,List<Integer> permissionIds);

    String getRoleOfPermissionList();

    String delRoleOfPermission(Integer roleId);

    String getUserOfPermissionOfThree(Integer roleId);
}
