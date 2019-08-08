package com.supervision.user.service.impl;

import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.StaticProperties;
import com.supervision.user.mapper.DepartmentMapper;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.model.Department;
import com.supervision.user.model.User;
import com.supervision.user.service.DepartmentService;
import com.supervision.user.util.ErrorCode;
import com.supervision.user.util.TypeString;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @ClassName DepartmentServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/15 15:02
 **/
@Service
public class DepartmentServiceImpl implements DepartmentService {

    JsonUtil jsonUtil = new JsonUtil();
    String result;

    @Resource
    DepartmentMapper departmentMapper;

    @Resource
    UserMapper userMapper;

    @Override
    public String addDepartment(Department department) {
        int count = departmentMapper.isExistDept(department.getDepartment());
        if (count > 0) {
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "部门已存在，请重新添加", "");
        } else {
            departmentMapper.insertSelective(department);
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, "");
        }
        return result;
    }

    @Override
    public Department getDepartment(Integer departmentId) {
        return departmentMapper.selectByPrimaryKey(departmentId);
    }

    @Override
    public String getDepartmentByCode(User user) {
        String workCode = user.getWorkCode();
        String userRealname = user.getUserRealname();
        if (workCode == null && userRealname == null){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"请输入查询条件","");
            return result;
        }
        List<User> userList = userMapper.getDepartmentId(workCode,userRealname);
        List list = new ArrayList();
        for (User user1 : userList){
            HashMap<String,String> map = new HashMap<>();
            Integer id = user1.getDepartmentId();
            Department data = departmentMapper.selectByPrimaryKey(id);
            String userName = user1.getUserRealname();
            String departmentName = data.getDepartment();
            String workCode1 = user1.getWorkCode();
            map.put("userRealName",userName);
            map.put("departmentName",departmentName);
            map.put("workCode",workCode1);
            list.add(map);
        }
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
        return result;
    }

    @Override
    public String getAllDepartment() {
       List<Department>  departmentList = departmentMapper.findAllDepartment();
       result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,departmentList);
       return result;
    }

    @Override
    public String delDept(Integer deptId) {
        departmentMapper.deleteByPrimaryKey(deptId);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getDeptOfUser() {
        List<Department> departmentList = departmentMapper.findAllDepartment();
        List list = new ArrayList();
        for (Department department : departmentList){
            HashMap map = new HashMap();
            String deptDesc = department.getDepartment();
            Integer deptId = department.getId();
            List<User> userList = userMapper.getUserByDeptId(deptId);
            List list1 = new ArrayList();
            for (User user : userList){
                HashMap map1 = new HashMap();
                String userName = user.getUserRealname();
                String workCode = user.getWorkCode();
                map1.put("userName",userName);
                map1.put("workCode",workCode);
                list1.add(map1);
            }
            map.put("userInfo",list1);
            map.put("deptDesc",deptDesc);
            list.add(map);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

    @Override
    public String addDept(Department department) {
        departmentMapper.insert(department);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String modifyDept(Department department) {
        departmentMapper.updateByPrimaryKey(department);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getOrganization() {
        List<Department> departmentList = departmentMapper.findAllDepartment();
        List list = new ArrayList();
        for (Department department :departmentList){
            HashMap map = new HashMap();
            Integer deptId = department.getId();
            String deptDesc = department.getDepartment();
            if (department.getType().equals(TypeString.DEPARTMENT)) {
                List<User> userList = userMapper.getUserByDeptId(deptId);
                List list1 = new ArrayList();
                for (User user : userList) {
                    HashMap map1 = new HashMap();
                    String workCode = user.getWorkCode();
                    String userName = user.getUserRealname();
                    map1.put("userName", userName);
                    map1.put("workCode", workCode);
                    list1.add(map1);
                }
                map.put("deptDesc",deptDesc);
                map.put("userInfo",list1);
                map.put("deptId",deptId);
                list.add(map);
            }
            if (department.getType().equals(TypeString.company)){
                map.put("company",department);
                list.add(map);
            }
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

    @Override
    public String getAllDeptInfo() {
        List<Department> departmentList = departmentMapper.findAllDepartment();
        List list = new ArrayList();
        for (Department department : departmentList){
            HashMap map = new HashMap();
            Integer deptId = department.getId();
            int number = userMapper.countUserByDeptId(deptId);
            String deptDesc = department.getDepartment();
            String type = department.getType();
            map.put("deptId",deptId);
            map.put("type",type);
            map.put("desc",deptDesc);
            map.put("number",number);
            list.add(map);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

}
