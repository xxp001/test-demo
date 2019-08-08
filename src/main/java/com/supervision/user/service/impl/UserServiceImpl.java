package com.supervision.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.MD5Util;
import com.supervision.common.util.StaticProperties;
import com.supervision.project.mapper.ProjectMapper;
import com.supervision.project.mapper.ProjectTeamMapper;
import com.supervision.user.mapper.*;
import com.supervision.user.model.*;
import com.supervision.user.service.UserService;
import com.supervision.user.util.ErrorCode;
import com.supervision.user.util.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author fangyong
 * @Date 2019/2/1 13:31
 **/
@Service
public class UserServiceImpl implements UserService {

    JsonUtil jsonUtil = new JsonUtil();
    String result;

    @Resource
    UserMapper userMapper;

    @Resource
    DepartmentMapper departmentMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired  //这个其实是角色和权限的映射表，懒得改了
    private UserPermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private ProjectTeamMapper projectTeamMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Resource
    UserDetailMapper userDetailMapper;

    @Override
    public Object getUserByPage(int currentPage, int pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        List<User> userList = userMapper.selectNotLogicDelete();
        int total = userMapper.countUser();
        int totalPage = (total + (pageSize-1))/pageSize;
        List list = new ArrayList();
        for (User user : userList) {
            HashMap<String,Object> map = new HashMap<>();
            HashMap<String, String> map1 = new HashMap<>();
            HashMap<String, String> map2 = new HashMap<>();
            HashMap<String, String> map3 = new HashMap<>();
            Integer id = user.getId();
            Integer roleId = user.getRoleId();
            map1.put("roleId","0");
            map1.put("roleDesc","暂无角色");
            if (roleId != 0) {
                Role role = roleMapper.selectRoleById(roleId);
                if (role != null) {
                    map1.put("roleId", String.valueOf(role.getId()));
                    map1.put("roleDesc", role.getRoledesc());
                }
            }
            Integer departmentId = user.getDepartmentId();
            Department department = departmentMapper.selectByPrimaryKey(departmentId);
            if (department != null) {
                map2.put("departmentId", String.valueOf(department.getId()));
                map2.put("departmentName",department.getDepartment());
            } else {
                map2.put("departmentId", "-1");
                map2.put("departmentName", "无部门");
            }
            String mobile = user.getMobile();
            String userRealname = user.getUserRealname();
            String workCode = user.getWorkCode();
            List list2 = new ArrayList();
            int count = rolePermissionMapper.getPermissionCountByRoleId(String.valueOf(roleId));
            if (roleId == 0) {
                map3.put("permissionId", "0");
                map3.put("permissionDesc", "暂无权限");
                list2.add(map3);
            }else if (count == 0){
                map3.put("permissionId", "0");
                map3.put("permissionDesc", "暂无权限");
                list2.add(map3);
            }else {
                List<UserPermission> userPermissionList = rolePermissionMapper.getUserPermissionId(roleId);
                for (UserPermission userPermission : userPermissionList) {
                    HashMap<String,String> map4 = new HashMap<>();
                    Integer permissionId = userPermission.getPid();
                    Permission permission1 = permissionMapper.selectPermissionById(permissionId);
                    map4.put("permissionId", String.valueOf(permission1.getId()));
                    map4.put("permissionDesc",permission1.getPermission());
                    list2.add(map4);
                }
            }
            map.put("id",id);
            map.put("workCode",workCode);
            map.put("department",map2);
            map.put("mobile",mobile);
            map.put("userRealname",userRealname);
            map.put("role",map1);
            map.put("permission",list2);
            list.add(map);
        }
        PageBean pageBean = new PageBean();
        pageBean.setItems(list);
        pageBean.getCurrentPage();
        pageBean.setPageSize(pageSize);
        pageBean.setTotal(total);
        pageBean.setTotalPage(totalPage);
        return pageBean;
    }

    @Override
    public String addUser(Map<String, String> user, int length) {
        if (user.isEmpty()){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"请勿添加空数据","");
        }
        String workCode = user.get("workCode");
        int count = userMapper.countUserByWorkcode(workCode);
        if (count > 0){
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "存在工号已被利用,请重新分配", "");
            return result;
        }else {
            String dept = user.get("dept");
            String DepartmentId = departmentMapper.getDepartmentIdByName(dept);
            User user1 = new User();
            UserDetail userDetail = new UserDetail();
            user1.setUserRealname(user.get("name"));
            user1.setWorkCode(user.get("workCode"));
            user1.setDepartmentId(Integer.valueOf(DepartmentId));
            user1.setMobile(user.get("mobile"));
            user1.setPassword(MD5Util.makeMD5(user.get("pwd")));
            user1.setRoleId(0);
            user1.setUserState((short) 1);
            userDetail.setWorkCode(user.get("workCode"));
            userDetail.setAge(Integer.valueOf(user.get("age")));
            userDetail.setEmail(user.get("email"));
            userDetail.setGender(user.get("gender"));
            userDetail.setIceName(user.get("ICEName"));
            userDetail.setIcePhone(user.get("ICEPhone"));
            userMapper.insert(user1);
            userDetailMapper.insert(userDetail);
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, ErrorCode.Error_CODE002, "");
        }
        return result;
    }

    @Override
    public String delUser(Integer id) {
//        userMapper.deleteByPrimaryKey(id);   不可以直接删除数据库记录，使用逻辑删除

        User user = userMapper.selectByPrimaryKey(id);
        short state = 2;  //表示已经逻辑删除
        user.setUserState(state);
        user.setRoleId(0);  //角色设置为0，无角色
        user.setDepartmentId(0); //部门id设置为0，无部门
        user.setUserRealname(user.getUserRealname() + "(已离职)");
        userMapper.updateByPrimaryKeySelective(user);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, ErrorCode.Error_CODE003, "");
        return result;
    }

    @Override
    public String getUserInfo(HttpServletRequest request, String workCode, String password) {
        User user = new User();
        user.setWorkCode(workCode);
        user.setPassword(password);
        HttpSession session = request.getSession();

        if (StringUtil.isEmpty(workCode) || StringUtil.isEmpty(password)) {
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "工号或密码不能为空!", "");
            return result;
        }
        User userQuery = userMapper.selectUserByWorkcode(workCode, password);
        if (userQuery == null) {
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "员工不存在", "");
            return result;
        }

        //执行到这里表示用户登录成功，将用户信息存到session
        session.setAttribute(StaticProperties.SESSION_USER, userQuery);

        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, "登录成功", "");
        return result;
    }

    @Override
    public String disableAccount(String workCode) {
        userMapper.disableAccount(workCode);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, "账号已被冻结", "");
        return result;
    }

    @Override
    public String enableAccount(String workCode) {
        userMapper.enableAccount(workCode);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, "账号已被启用", "");
        return result;
    }

    @Override
    public String transferUser(String workCode, Integer departmentId) {
        userMapper.transferUser(workCode, departmentId);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, ErrorCode.Error_CODE005, "");
        return result;
    }

    @Override
    public User getUser(int userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        user.setPassword("");
        return user;
    }

    @Override
    public User getUserByWorkCode(String workCode) {
        return userMapper.selectByWorkCodeOnly(workCode);
    }

    @Override
    public Set<String> getRolesStrByUserId(int userId) {

        //首先根据用户id查询出用户，找出用户的roleId
        Integer roleId = userMapper.selectByPrimaryKey(userId).getRoleId();

        //根据roleId找出role的名字
        String roledesc = roleMapper.selectRoleById(roleId).getRoledesc();

        Set<String> rolesStr = new LinkedHashSet<>();
        rolesStr.add(roledesc);

        return rolesStr;
    }

    @Override
    public Set<String> getPermissionsStrByUserId(int userId) {

        //首先根据用户id查询出用户，找出用户的roleId
        Integer roleId = userMapper.selectByPrimaryKey(userId).getRoleId();

        //根据roleId找出所有的权限
        List<UserPermission> rolePermissions = rolePermissionMapper.getUserPermissionId(roleId);

        Set<String> permissionsStr = new LinkedHashSet<>();

        for (int i = 0; i < rolePermissions.size(); i++) {
            permissionsStr.add(permissionMapper.selectPermissionById(rolePermissions.get(i).getPid()).getPermission());
        }

        return permissionsStr;
    }

    @Override
    public boolean setUserDirector(Integer userId) {

        User user = new User();
        user.setId(userId);
        user.setRoleId(10);  //设置为项目总监
        int row = userMapper.updateByPrimaryKeySelective(user);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setUserDelegate(Integer userId) {

        User user = new User();
        user.setId(userId);
        user.setRoleId(5);  //设置为总监代表
        int row = userMapper.updateByPrimaryKeySelective(user);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setUserNormal(Integer userId) {

        User user = new User();
        user.setId(userId);
        user.setRoleId(1);  //设置为监理工程师
        int row = userMapper.updateByPrimaryKeySelective(user);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public String delUserOfRole(String workCode) {
        userMapper.delUserOfRole(workCode);
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
        return result;
    }

    @Override
    public List<User> getAllDirector() {
        return userMapper.selectDirectors();
    }

    @Override
    public List<User> getAllDelegates() {
        return userMapper.selectDelegates();
    }

    @Override
    public List<User> getAllEngineers() {
        return userMapper.selectEngineers();
    }

    @Override
    public String modifyPwd(String workCode, String password) {
        List<User> userList = userMapper.getUserInfoByWorkcode(workCode);
        for (User user : userList){
            if (user.getUserState() == 1){
                userMapper.modifyPwd(workCode,password);
                result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
            }else {
                result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"此工号已被冻结,请先启用","");
            }
        }
        return result;
    }

    @Override
    public String getUserInfoByWorkCode(String workCode) {
        int count = userMapper.countUserByWorkcode(workCode);
        if (count > 0){
            List<User> userList = userMapper.getUserInfoByWorkcode(workCode);
            HashMap<String, String> map = new HashMap<>();
            for (User user : userList){
                String name = user.getUserRealname();
                String mobile = user.getMobile();
                Department department = departmentMapper.selectByPrimaryKey(user.getDepartmentId());
                String departmentName = department.getDepartment();
                UserDetail userDetail = userDetailMapper.selectUserDetailInfo(workCode);
                Integer age = userDetail.getAge();
                String gender = userDetail.getGender();
                String email = userDetail.getEmail();
                String iceName = userDetail.getIceName();
                String icePhone = userDetail.getIcePhone();
                map.put("workCode",workCode);
                map.put("name",name);
                map.put("mobile",mobile);
                map.put("dept",departmentName);
                map.put("age", String.valueOf(age));
                map.put("gender",gender);
                map.put("email",email);
                map.put("iceName",iceName);
                map.put("icePhone",icePhone);
                result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,map);
            }
        }else {
            result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,"查无此工号","");
        }
        return result;
    }

    @Override
    public String getUserListOfRole(Integer roleId) {
        List<User> userList = userMapper.getUserInfoByRoleId(roleId);
        List list = new ArrayList();
        for (User user : userList){
            HashMap map = new HashMap();
            String userName = user.getUserRealname();
            String workCode = user.getWorkCode();
            Integer deptId = user.getDepartmentId();
            Role role = roleMapper.selectRoleById(roleId);
            String roleDesc = role.getRoledesc();
            Department department = departmentMapper.selectByPrimaryKey(deptId);
            String deptDesc = department.getDepartment();
            map.put("workCode",workCode);
            map.put("userName",userName);
            map.put("dept",deptDesc);
            map.put("roleDesc",roleDesc);
            list.add(map);
        }
        result = jsonUtil.JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
        return result;
    }

    @Override
    public String checkUserInfoByWorkCode(String workCode) {
        HashMap map = new HashMap();
        User userPO = userMapper.selectByWorkCodeOnly(workCode);
        Integer deptId = userPO.getDepartmentId();
        Integer roleId = userPO.getRoleId();
        Department department = departmentMapper.selectByPrimaryKey(deptId);
        Role role = roleMapper.selectRoleById(roleId);
        String deptDesc = department.getDepartment();
        String roleDesc = role.getRoledesc();
        map.put("id",userPO.getId());
        map.put("workCode",workCode);
        map.put("userName",userPO.getUserRealname());
        map.put("deptDesc",deptDesc);
        map.put("roleDesc",roleDesc);
        map.put("phone",userPO.getMobile());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,map);
    }

    @Override
    public String getUserInfoByCompanyId(Integer deptId) {
        List list = new ArrayList();
        List<User> userList = userMapper.getUserByDeptId(deptId);
        for (User user : userList){
            HashMap map = new HashMap();
            Integer roleId= user.getRoleId();
            Role role = roleMapper.selectRoleById(roleId);
            map.put("workCode",user.getWorkCode());
            map.put("userName",user.getUserRealname());
            map.put("roleDesc",role.getRoledesc());
            map.put("phone",user.getMobile());
            list.add(map);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

    @Override
    public String modifyUserInfoDept(String workCode, String userName, Integer deptId, String phone) {
        userMapper.modifyDeptOfUserInfo(workCode,userName,deptId,phone);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String modifyUserInfoCompany(String workCode, String userName, String phone) {
        userMapper.modifyUserInfoCompany(workCode,userName,phone);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public void updateUserLoginOutState(String workCode) {
        userMapper.updateUserLoginOutState(workCode);
    }

    @Override
    public String delUserFromDept(String workCode) {
        User user = userMapper.selectByWorkCodeOnly(workCode);
        Integer deptId = 0;
        user.setDepartmentId(deptId);
        user.setRoleId(0);
        userMapper.updateByPrimaryKey(user);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String updateUserInfo(String workCode, String mobile, Integer age, String email, String iceName, String icePhone) {
        userMapper.updateUserPhone(workCode,mobile);
        userDetailMapper.updateUserDetailInfo(workCode,age,email,iceName,icePhone);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    @Override
    public String getUserInfoByDeptId(Integer deptId) {
        List<User> userList = userMapper.getUserByDeptId(deptId);
        List list = new ArrayList();
        for (User user : userList){
            if (user.getUserState()==1) {
                HashMap map = new HashMap();
                String workCode = user.getWorkCode();
                String userName = user.getUserRealname();
                Department dept = departmentMapper.selectByPrimaryKey(deptId);
                Role role = roleMapper.selectRoleById(user.getRoleId());
                String roleDesc = role.getRoledesc();
                String deptDesc = dept.getDepartment();
                String phone = user.getMobile();
                map.put("workCode", workCode);
                map.put("userName", userName);
                map.put("deptDesc", deptDesc);
                map.put("roleDesc", roleDesc);
                map.put("phone", phone);
                list.add(map);
            }
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,list);
    }

}
