package com.supervision.user.service.impl;

import com.github.pagehelper.util.StringUtil;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.StaticProperties;
import com.supervision.user.mapper.PermissionMapper;
import com.supervision.user.mapper.RoleMapper;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.mapper.UserPermissionMapper;
import com.supervision.user.model.Permission;
import com.supervision.user.model.Role;
import com.supervision.user.model.UserPermission;
import com.supervision.user.service.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName PermissionServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/18 18:24
 **/
@Service
public class PermissionServiceImpl implements PermissionService {

    JsonUtil jsonUtil = new JsonUtil();
    String result;

    @Resource
    PermissionMapper permissionMapper;

    @Resource
    UserPermissionMapper userPermissionMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    RoleMapper roleMapper;

    @Override
    public String setPermission(Permission permission) {
        int count = permissionMapper.getPermissionCount(permission);
        if (StringUtil.isEmpty(permission.getPermission()) && permission.getId() == null){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"字段不能为空","");
            return result;
        }else if (count > 0){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"已存在该权限","");
            return result;
        }
        permissionMapper.insertSelective(permission);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
        return result;
    }

    @Override
    public String assignPermissions(Integer roleId, String[] permissionIds) {
        Role role = roleMapper.selectRoleById(roleId);
        roleId = role.getId();
        List<String> list = Arrays.asList(permissionIds);
        for (String permissionId : list){
            Permission permission = permissionMapper.selectPermissionById(Integer.parseInt(permissionId));
            permissionId = String.valueOf(permission.getId());
            int count = userPermissionMapper.getRolePermissionInfo(roleId, Integer.valueOf(permissionId));
            if (count == 0){
                UserPermission userPermission = new UserPermission();
                userPermission.setRid(roleId);
                userPermission.setPid(Integer.valueOf(permissionId));
                userPermissionMapper.insert(userPermission);
                result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
            }else {
                result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, "");
            }
        }
        return result;
    }

    @Override
    public String modifyPermission(Permission permission) {
        permissionMapper.modifyPermission(permission);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
        return result;
    }

    @Override
    public String getPermissionList() {
        List<Permission> permissionList = permissionMapper.getPermissionList();
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,permissionList);
        return result;
    }
}
