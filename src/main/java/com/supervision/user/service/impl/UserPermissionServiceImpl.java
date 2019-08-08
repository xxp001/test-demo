package com.supervision.user.service.impl;

import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.StaticProperties;
import com.supervision.user.mapper.PermissionMapper;
import com.supervision.user.mapper.RoleMapper;
import com.supervision.user.mapper.UserPermissionMapper;
import com.supervision.user.model.Permission;
import com.supervision.user.model.Role;
import com.supervision.user.model.UserPermission;
import com.supervision.user.service.UserPermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName UserPermissionServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/18 21:01
 **/
@Service
public class UserPermissionServiceImpl implements UserPermissionService {

    @Resource
    UserPermissionMapper userPermissionMapper;

    @Resource
    RoleMapper roleMapper;

    @Resource
    PermissionMapper permissionMapper;


    @Override
    public String getRoleOfPermission(Integer roleId) {
        List<UserPermission> userPermissionList = userPermissionMapper.getUserPermissionId(roleId);
        Set set = new HashSet();
        for (UserPermission userPermission : userPermissionList){
            Integer permissionId = userPermission.getPid();
            Permission permission = permissionMapper.selectPermissionById(permissionId);
            set.add(permission);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,set);
    }

    @Override
    public String modifyRoleOfPermission(Integer roleId,List<Integer> permissionIds) {
        userPermissionMapper.delRoleOfPermission(roleId);
        UserPermission userPermission = new UserPermission();
        for (Integer permissionId : permissionIds){
            userPermission.setRid(roleId);
            userPermission.setPid(permissionId);
            userPermissionMapper.insertSelective(userPermission);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getRoleOfPermissionList() {
        List<Role>  roleList = roleMapper.getAllRole();
        List list = new ArrayList();
        for (Role role : roleList){
            HashMap map = new HashMap();
            Role role1 = new Role();
            Integer roleId = role.getId();
            String roleDesc = role.getRoledesc();
            role1.setId(roleId);
            role1.setRoledesc(roleDesc);
            List<Permission> permissionList = permissionMapper.getPermissionByRoleId(roleId);
            map.put("roleData",role1);
            map.put("permissionData",permissionList);
            list.add(map);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

    @Override
    public String delRoleOfPermission(Integer roleId) {
        userPermissionMapper.delRoleOfPermission(roleId);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getUserOfPermissionOfThree(Integer roleId) {
        List<UserPermission> userPermissionList = userPermissionMapper.getUserPermissionId(roleId);
        List list = new ArrayList();
        for (UserPermission userPermission : userPermissionList){
            Integer permissionId = userPermission.getPid();
            if (permissionId==8 || permissionId==9 || permissionId==10){
                HashMap map = new HashMap();
                Permission permissionPO = permissionMapper.selectPermissionById(permissionId);
                String permission = permissionPO.getPermission();
                String permissionDesc = permissionPO.getPermissionDesc();
                map.put("permission",permission);
                map.put("permissionDesc",permissionDesc);
                list.add(map);
            }
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }
}
