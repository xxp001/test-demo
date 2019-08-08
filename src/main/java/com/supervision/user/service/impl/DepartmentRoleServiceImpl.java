package com.supervision.user.service.impl;

import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.StaticProperties;
import com.supervision.user.mapper.DepartmentMapper;
import com.supervision.user.mapper.DepartmentRoleMapper;
import com.supervision.user.mapper.RoleMapper;
import com.supervision.user.model.Department;
import com.supervision.user.model.DepartmentRole;
import com.supervision.user.model.Role;
import com.supervision.user.service.DepartmentRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName DepartmentRoleServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/4/29 13:02
 **/
@Service
public class DepartmentRoleServiceImpl implements DepartmentRoleService {

    @Resource
    DepartmentRoleMapper departmentRoleMapper;

    @Resource
    DepartmentMapper departmentMapper;

    @Resource
    RoleMapper roleMapper;

    @Override
    public String delDeptOfRole(Integer deptId) {
        departmentRoleMapper.deleteByPrimaryKey(deptId);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String modifyDeptOfRole(Integer deptId, List<Integer> roleIds) {
        departmentRoleMapper.deleteByPrimaryKey(deptId);
        DepartmentRole departmentRole = new DepartmentRole();
        for (Integer roleId : roleIds){
            departmentRole.setDeptId(deptId);
            departmentRole.setRoleId(roleId);
            departmentRoleMapper.insert(departmentRole);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getOrganizationInfo() {
        List<Department> departmentList = departmentMapper.findAllDepartment();
        List list = new ArrayList();
        for (Department departmentPO : departmentList){
            HashMap map = new HashMap();
            Integer deptId = departmentPO.getId();
            List<Role> roleList = roleMapper.getRoleByDeptId(deptId);
            map.put("departmentData",departmentPO);
            map.put("roleData",roleList);
            list.add(map);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }
}
