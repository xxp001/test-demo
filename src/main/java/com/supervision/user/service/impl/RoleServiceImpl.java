package com.supervision.user.service.impl;

import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.StaticProperties;
import com.supervision.project.mapper.RoleCostMapper;
import com.supervision.project.model.RoleCost;
import com.supervision.user.mapper.PermissionMapper;
import com.supervision.user.mapper.RoleMapper;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.mapper.UserPermissionMapper;
import com.supervision.user.model.Permission;
import com.supervision.user.model.Role;
import com.supervision.user.model.User;
import com.supervision.user.model.UserPermission;
import com.supervision.user.service.RoleService;
import com.supervision.user.util.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @ClassName RoleServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/3 14:46
 **/
@Service
public class RoleServiceImpl implements RoleService {

    JsonUtil jsonUtil = new JsonUtil();
    String result;

    @Resource
    RoleMapper roleMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    PermissionMapper permissionMapper;

    @Resource
    UserPermissionMapper userPermissionMapper;

    @Autowired
    RoleCostMapper roleCostMapper;

    @Override
    public String assignRole(String workCode, Integer roleId) {
        Role role = roleMapper.selectRoleById(roleId);
        if (role != null){
            roleMapper.setRole(workCode, String.valueOf(roleId));
            Set set = new HashSet();
            HashMap map = new HashMap();
            List<UserPermission> userPermissionList = userPermissionMapper.getUserPermissionId(roleId);
            for (UserPermission userPermission : userPermissionList){
               Integer permissionId = userPermission.getPid();
               Permission permissionPO = permissionMapper.selectPermissionById(permissionId);
               String permission = permissionPO.getPermission();
               set.add(permission);
            }
            map.put("role",role);
            map.put("permission",set);
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, "分配成功", map);
            return result;
        }
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"暂无该角色","");
        return result;
    }

    @Transactional
    @Override
    public String addRole(String roleDesc) {
        String roleId = roleMapper.getRoleIdByRoledesc(roleDesc);
        if (roleId == null){
            Role role = new Role();
            role.setRoledesc(roleDesc);
            roleMapper.insertSelective(role);

            // 同时设置角色成本
            RoleCost roleCost = new RoleCost();
            roleCost.setRoleId(role.getId());
            roleCost.setRoleCostHalfDay(new BigDecimal(0));
            roleCost.setRoleCostDesc(role.getRoledesc());
            roleCostMapper.insertSelective(roleCost);

            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
        }else if (roleId != null){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"角色已存在，请重新添加","");
        }
        return result;
    }

    @Override
    public String delRole(Integer id) {
        Integer roleId = id;
        List<User> userList = userMapper.getUserInfoByRoleId(roleId);
        if (userList.size() > 0){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"该角色下存在用户,请先解除关联","");
            return result;
        }
        roleMapper.delRole(id);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
        return result;
    }

    @Override
    public String modifyRole(Role role) {
        roleMapper.modifyRole(role);
        result = jsonUtil.JsonInfo(1,ErrorCode.Error_CODE005,"");
        return result;
    }

    @Override
    public String getAllRole() {
        List<Role> roleList = roleMapper.getAllRole();
        List list = new ArrayList();
        for (Role role : roleList){
            HashMap<String, String> map1 = new HashMap<>();
            Integer roleId = role.getId();
            String roles = role.getRoledesc();
            List<User> userList = userMapper.getUserInfoByRoleId(roleId);
            List<UserPermission> userPermissionList = userPermissionMapper.getUserPermissionId(roleId);
            for (UserPermission userPermission : userPermissionList){
                Integer permissionId = userPermission.getPid();
                List<Permission> permissionList = permissionMapper.getPermissionById(permissionId);
                for (Permission permission1 : permissionList){
                    String permission = permission1.getPermission();
                    map1.put("role",roles);
                    map1.put("permission",permission);
                }
                for (User user : userList){
                    String userRealname = user.getUserRealname();
                    map1.put("userRealname",userRealname);
                }
            }

            list.add(map1);
        }
        HashMap<String, List> map = new HashMap<>();
        map.put("data",list);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,map);
        return result;
    }

    @Override
    public Role getRoleById(Integer roleId) {
        return roleMapper.selectRoleById(roleId);
    }

    @Override
    public String getRoleList() {
        List<Role> roleList = roleMapper.getAllRole();
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,roleList);
        return result;
    }
}
