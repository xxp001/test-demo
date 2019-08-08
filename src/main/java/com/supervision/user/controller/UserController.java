package com.supervision.user.controller;


import com.supervision.common.util.*;
import com.supervision.user.model.Department;
import com.supervision.user.model.Permission;
import com.supervision.user.model.Role;
import com.supervision.user.model.User;
import com.supervision.user.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author fangyong
 * @Date 2019/1/31 19:53
 **/

@RestController
@RequestMapping("/user")
@Api(tags = "用户API")
@CrossOrigin
public class UserController {

    @Resource
    UserService userService;

    @Resource
    RoleService roleService;

    @Resource
    DepartmentService departmentService;

    @Resource
    PermissionService permissionService;

    @Resource
    UserPermissionService userPermissionService;

    @Resource
    SessionDAO sessionDAO;

    @Resource
    DepartmentRoleService departmentRoleService;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * @Author fangyong
     * @Description 查看所有用户,分页查询
     * @Date 2019/1/31 19:59
     * @Param
     * @return
     **/
    @ApiOperation(value = "分页查询员工" ,  notes="分页查询员工")
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_USER_QUERY})
    @RequestMapping(value = "/getAll",method = {RequestMethod.POST, RequestMethod.GET})
    public String userPage(Integer currentPage, Integer pageSize){
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, userService.getUserByPage(currentPage,pageSize));
    }

    /**
     * @Author fangyong
     * @Description 新增员工
     * @Date 2019/2/2 13:32
     * @Param
     * @return
     **/
    @ApiOperation(value = "批量分配账号" ,  notes="批量分配账号")
    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_USER_ADD})
    public String addUser(@RequestBody List<Map<String,String>> user){
        System.out.println(user);
        String result = "";
        int length =user.size();
        for (Map<String,String> userMap : user){
            System.out.println(userMap);
            result = userService.addUser(userMap,length);
        }
        return result;
    }
    /**
     * @Author fangyong
     * @Description 修改密码
     * @Date 2019/2/23 17:32
     * @Param
     * @return
     **/
    @ApiOperation(value = "修改密码" ,  notes="修改密码")
    @RequestMapping(value = "/modifyPassword", method = RequestMethod.POST)
    public String modifyPwd(HttpServletRequest request,String workCode, String password){
        HttpSession session = request.getSession();
        User userInfo = (User) session.getAttribute(StaticProperties.SESSION_USER);
        workCode = userInfo.getWorkCode();
        String MD5Password = MD5Util.makeMD5(password);
        return userService.modifyPwd(workCode,MD5Password);
    }

    /**
     * @Author fangyong
     * @Description 删除员工
     * @Date 2019/2/2 15:38
     * @Param
     * @return
     **/
    @ApiOperation(value = "删除员工" ,  notes="删除员工")
    @RequestMapping(value = "/delUser",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_USER_DELETE})
    public String delUser(Integer id){
        return userService.delUser(id);
    }

    /**
     * @Author fangyong
     * @Description 员工登录
     * @Date 2019/2/3 11:31
     * @Param
     * @return
     **/
    @ApiOperation(value = "登录" ,  notes="登录")
    @CrossOrigin
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public String login(HttpServletRequest request, String workCode, String password) {
        String MD5Password = MD5Util.makeMD5(password);
        try {
            UsernamePasswordToken token = new UsernamePasswordToken(workCode, MD5Password);
            org.apache.shiro.subject.Subject subject = SecurityUtils.getSubject();
            subject.login(token);
            User user = userService.getUserByWorkCode(workCode);
            Collection<Session> sessions = sessionDAO.getActiveSessions();
            if (subject.isAuthenticated()) {
                for (Session session : sessions) {
                    if (workCode.equals(session.getAttribute(StaticProperties.SESSION_USER))) {
                        subject.logout();
                        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, "用户已登录");
                    }
                }
            }
            subject.getSession().setTimeout(1000*60*30);
            request.getSession().setAttribute(StaticProperties.SESSION_USER, user);
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, user);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @Author fangyong
     * @Description 判断是否登录，并返回登录用户的信息
     * @Date 2019/3/27 23:02
     * @Param
     * @return
     **/
    @ApiOperation(value = "返回登录用户的信息" ,  notes="返回登录用户的信息")
    @RequestMapping(value = "/getLoginUserInfo",method = {RequestMethod.POST,RequestMethod.GET})
    public String getLoginUserInfo(HttpServletRequest request){
        HttpSession session = request.getSession();
        if(session.getAttribute(StaticProperties.SESSION_USER) == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, "用户未登录");
        }else if (request.getSession(false) == null){
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, "session已过期");
        }
        User userInfo = (User) session.getAttribute(StaticProperties.SESSION_USER);
        User user = new User();
        user.setUserRealname(userInfo.getUserRealname());
        user.setMobile(userInfo.getMobile());
        user.setWorkCode(userInfo.getWorkCode());
        user.setUserState(userInfo.getUserState());
        user.setId(userInfo.getId());
        user.setRoleId(userInfo.getRoleId());
        user.setDepartmentId(userInfo.getDepartmentId());
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,user);
    }

    /**
     * @Author fangyong
     * @Description 登出
     * @Date 2019/2/14 19:34
     * @Param
     * @return
     **/
    @ApiOperation(value = "注销" ,  notes="注销")
    @RequestMapping(value = "/loginOut",method = RequestMethod.POST)
    public String loginOut(HttpServletRequest request){
        SecurityUtils.getSubject().logout();
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,"注销成功","");
    }

    /**
     * @Author fangyong
     * @Description 分配角色
     * @Date 2019/2/3 14:40
     * @Param
     * @return
     **/
    @ApiOperation(value = "分配角色" ,  notes="分配角色")
    @RequestMapping(value = "/assignRole",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String assignRole(String workCode, Integer roleId){
        return roleService.assignRole(workCode,roleId);
    }


    /**
     * @Author fangyong
     * @Description 角色添加
     * @Date 2019/2/15 17:09
     * @Param
     * @return
     **/
    @ApiOperation(value = "角色添加" ,  notes="角色添加")
    @RequestMapping(value = "/addRole",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String addRole(String roleDesc){
        return roleService.addRole(roleDesc);
    }

    /**
     * @Author fangyong
     * @Description 角色删除
     * @Date 2019/2/15 17:42
     * @Param
     * @return
     **/
    @ApiOperation(value = "角色删除" ,  notes="角色删除")
    @RequestMapping(value = "/delRole",method = {RequestMethod.POST,RequestMethod.GET})
    @CrossOrigin
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String delRole(Integer id){
        return roleService.delRole(id);
    }

    /**
     * @Author fangyong
     * @Description 角色修改
     * @Date 2019/2/16 16:13
     * @Param
     * @return
     **/
    @ApiOperation(value = "角色修改" ,  notes="角色修改")
    @RequestMapping(value = "/modifyRole",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String modifyRole(Role role){
        return roleService.modifyRole(role);
    }

    /**
     * @Author fangyong
     * @Description 新增部门
     * @Date 2019/2/15 14:44
     * @Param
     * @return
     **/
    @ApiOperation(value = "新增部门" ,  notes="新增部门")
    @RequestMapping(value = "/addDepartment",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String addDepartment(Department department){
        return departmentService.addDepartment(department);
    }

    /**
     * @Author fangyong
     * @Description 冻结账号
     * @Date 2019/2/16 16:53
     * @Param
     * @return
     **/
    @ApiOperation(value = "冻结账号" ,  notes="冻结账号")
    @RequestMapping(value = "/disableAccount",method = RequestMethod.POST)
    public String disableAccount(String workCode){
        return userService.disableAccount(workCode);
    }

    /**
     * @Author fangyong
     * @Description 解冻账号
     * @Date 2019/2/16 17:16
     * @Param
     * @return
     **/
    @ApiOperation(value = "解冻账号" ,  notes="解冻账号")
    @RequestMapping(value = "/enableAccount",method = RequestMethod.POST)
    public String enableAccount(String workCode){
        return userService.enableAccount(workCode);
    }

    /**
     * @Author fangyong
     * @Description 人员调动
     * @Date 2019/2/16 17:33
     * @Param
     * @return
     **/
    @ApiOperation(value = "人员调动" ,  notes="人员调动")
    @RequestMapping(value = "/transferUser",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String transferUser(String workCode, Integer departmentId){
        return userService.transferUser(workCode,departmentId);
    }



    /**
     * @Author fangyong
     * @Description 设置权限
     * @Date 2019/2/17 16:02
     * @Param
     * @return
     **/
    @ApiOperation(value = "设置权限" ,  notes="设置权限")
    @RequestMapping(value = "/setPermission",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String setPermission(Permission permission){
        return permissionService.setPermission(permission);
    }

    /**
     * @Author fangyong
     * @Description 角色分配权限
     * @Date 2019/2/17 15:27
     * @Param
     * @return
     **/
    @ApiOperation(value = "角色分配权限" ,  notes="分配权限")
    @RequestMapping(value = "/assignPermissions",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String assignPermissions(Integer roleId, String[] permissionIds){
        return permissionService.assignPermissions(roleId,permissionIds);
    }

    /**
     * @Author fangyong
     * @Description 根据工号/姓名查询所属部门
     * @Date 2019/2/18 15:24
     * @Param
     * @return
     **/
    @ApiOperation(value = "根据工号/姓名查询所属部门" ,  notes="根据工号/姓名查询所属部门")
    @RequestMapping(value = "/getDepartmentByCode",method = RequestMethod.POST)
    public String getDepartmentByCode(User user){
        return departmentService.getDepartmentByCode(user);
    }

    /**
     * @Author fangyong
     * @Description 修改权限
     * @Date 2019/2/18 19:52
     * @Param
     * @return
     **/
    @ApiOperation(value = "修改权限" ,  notes="修改权限")
    @RequestMapping(value = "/modifyPermission",method = RequestMethod.POST)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String modifyPermission(Permission permission){
        return permissionService.modifyPermission(permission);
    }

    /**
     * @Author fangyong
     * @Description 编辑角色权限
     * @Date 2019/2/18 20:05
     * @Param
     * @return
     **/
    @ApiOperation(value = "编辑角色权限" ,  notes="编辑角色权限")
    @RequestMapping(value = "/modifyRoleOfPermission",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String modifyRoleOfPermission(Integer roleId, String permissionId){
        if(permissionId.equals("")){
            return userPermissionService.delRoleOfPermission(roleId);
        }
        List<Integer> permissionIds = new ArrayList<>();
        String[] buff = permissionId.split(",");
        for (int i = 0;i<buff.length;i++){
            permissionIds.add(Integer.valueOf(buff[i]));
        }
        return userPermissionService.modifyRoleOfPermission(roleId,permissionIds);
    }

    /**
     * @Author fangyong
     * @Description 获取角色权限(展示该角色下已拥有的权限选项)
     * @Date 2019/4/24 13:00
     * @Param
     * @return
     **/
    @ApiOperation(value = "获取角色已拥有的权限" ,  notes="获取角色已拥有的权限")
    @RequestMapping(value = "/getRoleOfPermission",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String getRoleOfPermission(Integer roleId){
        return userPermissionService.getRoleOfPermission(roleId);
    }

    /**
     * @Author fangyong
     * @Description 获取角色权限管理列表
     * @Date 2019/4/24 15:35
     * @Param
     * @return
     **/
    @ApiOperation(value = "获取角色权限管理列表" ,  notes="获取角色权限管理列表")
    @RequestMapping(value = "/getRoleOfPermissionList",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String getRoleOfPermissionList(){
        return userPermissionService.getRoleOfPermissionList();
    }


    /**
     * @Author fangyong
     * @Description 查询所有角色以及角色对应的人员
     * @Date 2019/2/18 23:02
     * @Param
     * @return
     **/
    @ApiOperation(value = "查询所有角色以及角色对应的人员" ,  notes="查询所有角色以及角色对应的人员")
    @RequestMapping(value = "/getAllRole",method = RequestMethod.GET)
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String getAllRole(){
        return roleService.getAllRole();
    }
    
    /**
     * @Author fangyong
     * @Description 解除用户与角色之间的关联
     * @Date 2019/2/20 12:22 
     * @Param 
     * @return 
     **/
    @ApiOperation(value = "解除用户与角色之间的关联" ,  notes="解除用户与角色之间的关联")
    @RequestMapping(value = "/delUserOfRole",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String delUserOfRole(String workCode){
       return  userService.delUserOfRole(workCode);
    }

    /**
     * @Author fangyong
     * @Description 获取角色列表
     * @Date 2019/2/20 13:56
     * @Param
     * @return
     **/
    @ApiOperation(value = "获取角色列表" ,  notes="获取角色列表")
    @RequestMapping(value = "/getRoleList",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String getRoleList(){
        return roleService.getRoleList();
    }
    
    /**
     * @Author fangyong
     * @Description 获取权限列表
     * @Date 2019/2/20 16:08 
     * @Param 
     * @return 
     **/
    @ApiOperation(value = "获取权限列表" ,  notes="获取权限列表")
    @RequestMapping(value = "/getPermissionList",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_PERMISSION_MANAGE})
    public String getPermissionList(){
        return permissionService.getPermissionList();
    }

    /**
     * @Author fangyong
     * @Description 查询所有部门
     * @Date 2019/2/20 17:05
     * @Param
     * @return
     **/
    @ApiOperation(value = "查询所有部门" ,  notes="查询所有部门")
    @RequestMapping(value = "/getAllDepartment",method = RequestMethod.POST)
    public String getAllDepartment(){
        return departmentService.getAllDepartment();
    }
    
    /**
     * @Author fangyong
     * @Description 根据工号查找员工信息
     * @Date 2019/2/23 20:48 
     * @Param 
     * @return 
     **/
    @ApiOperation(value = "根据工号查找员工信息" ,  notes="根据工号查找员工信息")
    @RequestMapping(value = "/getUserInfoByWorkCode",method = RequestMethod.POST)
    public String getUserInfoByWorkCode(String workCode){
        return userService.getUserInfoByWorkCode(workCode);
    }

    /**
     * @Author fangyong
     * @Description 生成验证码
     * @Date 2019/3/20 17:10
     * @Param
     * @return
     **/
    @ApiOperation(value = "生成验证码" ,  notes="生成验证码")
    @CrossOrigin(allowCredentials = "true")
    @RequestMapping(value = "/getVerify",method={RequestMethod.POST,RequestMethod.GET})
    public String getVerify(HttpServletRequest request, HttpServletResponse response){
        try {
            response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
            response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Access-Control-Allow-Origin","true");
            response.setDateHeader("Expire", 0);
            RandomValidateCodeUtil randomValidateCode = new RandomValidateCodeUtil();
            randomValidateCode.getRandcode(request, response);//输出验证码图片方法
        }catch (Exception e){
            logger.error("获取验证码失败",e);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
    }

    /**
     * @Author fangyong
     * @Description 校验验证码
     * @Date 2019/3/20 17:21
     * @Param
     * @return
     **/
    @ApiOperation(value = "校验验证码" ,  notes="校验验证码")
    @CrossOrigin(allowCredentials = "true")
    @RequestMapping(value = "/checkVerify",method = {RequestMethod.POST,RequestMethod.GET})
    public String checkVerify(String inputStr, HttpSession session) {
        try{
            //从session中获取随机数
            String random = (String) session.getAttribute("RANDOMVALIDATECODEKEY");
            if (random == null) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,StaticProperties.RESPONSE_MESSAGE_FAIL,"");
            }
            if (random.equals(inputStr)) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,StaticProperties.RESPONSE_MESSAGE_SUCCESS,"");
            } else {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,StaticProperties.RESPONSE_MESSAGE_FAIL,"");
            }
        }catch (Exception e){
            logger.error("验证码校验失败",e);
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL,StaticProperties.RESPONSE_MESSAGE_FAIL,"");
        }
    }

    /**
     * @Author fangyong
     * @Description 部门下编辑角色
     * @Date 2019/4/29 13:03
     * @Param
     * @return
     **/
    @ApiOperation(value = "部门下编辑角色" ,  notes="部门下编辑角色")
    @RequestMapping(value = "/modifyDeptOfRole",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String modifyDeptOfRole(Integer deptId, String roleId){
        if (roleId.equals("")){
            return departmentRoleService.delDeptOfRole(deptId);
        }
        List<Integer> roleIds = new ArrayList<>();
        String[] buff = roleId.split(",");
        for (int i = 0;i<buff.length;i++){
            roleIds.add(Integer.valueOf(buff[i]));
        }
        return departmentRoleService.modifyDeptOfRole(deptId,roleIds);
    }


    /**
     * @Author fangyong
     * @Description 根据角色返回对应人员列表
     * @Date 2019/5/5 9:38
     * @Param
     * @return
     **/
    @ApiOperation(value = "根据角色返回对应人员列表" ,  notes="根据角色返回对应人员列表")
    @RequestMapping(value = "/getUserListOfRole",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_ROLE_MANAGE})
    public String getUserListOfRole(Integer roleId){
        return userService.getUserListOfRole(roleId);
    }

    /**
     * @Author fangyong
     * @Description 删除部门
     * @Date 2019/5/8 22:57
     * @Param
     * @return
     **/
    @ApiOperation(value = "删除部门" ,  notes="删除部门")
    @RequestMapping(value = "/delDept",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String delDept(Integer deptId){
        return departmentService.delDept(deptId);
    }

    /**
     * @Author fangyong
     * @Description
     * @Date 2019/5/8 23:55
     * @Param
     * @return
     **/
    @ApiOperation(value = "根据工号查询员工信息" ,  notes="根据工号查询员工信息")
    @RequestMapping(value = "/checkUserInfoByWorkCode",method = {RequestMethod.POST,RequestMethod.GET})
    public String checkUserInfoByWorkCode(String workCode){
        return userService.checkUserInfoByWorkCode(workCode);
    }

    /**
     * @Author fangyong
     * @Description 添加部门
     * @Date 2019/5/10 10:55
     * @Param
     * @return
     **/
    @ApiOperation(value = "添加部门" ,  notes="添加部门")
    @RequestMapping(value = "/addDept",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String addDept(Department department){
        return departmentService.addDept(department);
    }

    /**
     * @Author fangyong
     * @Description 编辑部门
     * @Date 2019/5/10 11:07
     * @Param
     * @return
     **/
    @ApiOperation(value = "编辑部门" ,  notes="编辑部门")
    @RequestMapping(value = "/modifyDept",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String modifyDept(Department department){
        return departmentService.modifyDept(department);
    }

    /**
     * @Author fangyong
     * @Description 查看组织架构
     * @Date 2019/5/10 11:18
     * @Param
     * @return
     **/
    @ApiOperation(value = "查看组织架构" ,  notes="查看组织架构")
    @RequestMapping(value = "/getOrganization",method = {RequestMethod.POST,RequestMethod.GET})
    public String getOrganization(){
        return departmentService.getOrganization();
    }
    
    /**
     * @Author fangyong
     * @Description 返回所有部门和所有分公司详细信息
     * @Date 2019/5/10 12:45 
     * @Param 
     * @return 
     **/
    @ApiOperation(value = "返回所有部门和所有分公司详细信息" ,  notes="返回所有部门和所有分公司详细信息")
    @RequestMapping(value = "/getAllDeptInfo",method = {RequestMethod.POST,RequestMethod.GET})
    public String getAllDeptInfo(){
        return departmentService.getAllDeptInfo();
    }

    /**
     * @Author fangyong
     * @Description 根据分公司id返回分公司下的员工信息
     * @Date 2019/5/10 12:57
     * @Param
     * @return
     **/
    @ApiOperation(value = "根据分公司id返回分公司下的员工信息" ,  notes="根据分公司id返回分公司下的员工信息")
    @RequestMapping(value = "/getUserInfoByCompanyId",method = {RequestMethod.POST,RequestMethod.GET})
    public String getUserInfoByCompanyId(Integer companyId){
        return userService.getUserInfoByCompanyId(companyId);
    }

    /**
     * @Author fangyong
     * @Description 编辑部门下员工资料
     * @Date 2019/5/10 14:01
     * @Param
     * @return
     **/
    @ApiOperation(value = "编辑部门下员工资料" ,  notes="编辑员工资料")
    @RequestMapping(value = "/modifyUserInfoDept",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String modifyUserInfoDept(String workCode,String userName,Integer deptId,String phone){
        return userService.modifyUserInfoDept(workCode,userName,deptId,phone);
    }

    /**
     * @Author fangyong
     * @Description 编辑分公司下员工资料
     * @Date 2019/5/10 14:01
     * @Param
     * @return
     **/
    @ApiOperation(value = "编辑分公司下员工资料" ,  notes="编辑分公司下员工资料")
    @RequestMapping(value = "/modifyUserInfoCompany",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String modifyUserInfoCompany(String workCode,String userName,String phone){
        return userService.modifyUserInfoCompany(workCode,userName,phone);
    }

    /**
     * @Author fangyong
     * @Description 从部门中剔除员工
     * @Date 2019/5/12 19:42
     * @Param
     * @return
     **/
    @ApiOperation(value = "从部门中剔除员工" ,  notes="从部门中剔除员工")
    @RequestMapping(value = "/delUserFromDept",method = {RequestMethod.POST,RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.SYSTEM_CONFIG_DEPARTMENT_MANAGE})
    public String delUserFromDept(String workCode){
        return userService.delUserFromDept(workCode);
    }

    /**
     * @Author fangyong
     * @Description 查看当前登录者的权限
     * @Date 2019/6/11 16:54
     * @Param
     * @return
     **/
    @ApiOperation(value = "查看当前登录者的权限" ,  notes="查看当前登录者的权限")
    @RequestMapping(value = "/getUserOfPermissionOfThree",method = {RequestMethod.POST,RequestMethod.GET})
    public String getUserOfPermissionOfThree(HttpServletRequest request){
        HttpSession session = request.getSession();
        User userInfo = (User) session.getAttribute(StaticProperties.SESSION_USER);
        Integer roleId = userInfo.getRoleId();
        return userPermissionService.getUserOfPermissionOfThree(roleId);
    }

    /**
     * @Author fangyong
     * @Description 编辑个人信息
     * @Date 2019/6/15 6:41
     * @Param
     * @return
     **/
    @ApiOperation(value = "编辑个人信息" ,  notes="编辑个人信息")
    @RequestMapping(value = "/updataUserInfo",method = {RequestMethod.POST,RequestMethod.GET})
    public String updataUserInfo(String workCode,String mobile, Integer age,String email,String iceName,String icePhone){
        return userService.updateUserInfo(workCode,mobile,age,email,iceName,icePhone);
    }

    /**
     * @Author fangyong
     * @Description 根据部门id返回对应部门下员工的信息
     * @Date 2019/7/4 17:40
     * @Param
     * @return
     **/
    @ApiOperation(value = "根据部门id返回对应部门下员工的信息" ,  notes="根据部门id返回对应部门下员工的信息")
    @RequestMapping(value = "/getUserInfoByDeptId",method = {RequestMethod.POST,RequestMethod.GET})
    public String getUserInfoByDeptId(Integer deptId){
        return userService.getUserInfoByDeptId(deptId);
    }
}
