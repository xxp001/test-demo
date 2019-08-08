package com.supervision.project.controller;

import com.supervision.common.util.*;
import com.supervision.project.ViewModel.ViewUser;
import com.supervision.project.model.ProjectTeam;
import com.supervision.project.service.ProjectTeamService;
import com.supervision.user.model.Department;
import com.supervision.user.model.User;
import com.supervision.user.service.DepartmentService;
import com.supervision.user.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/*
 * @Project:SupervisionSystem
 * @Description:project team controller
 * @Author:TjSanshao
 * @Create:2019-02-08 12:07
 *
 **/
@Controller
@CrossOrigin
@RequiresAuthentication
public class ProjectTeamController {

    @Autowired
    private ProjectTeamService projectTeamService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    //查看项目团队，只有高级权限或者参与了项目才允许查看
    @ResponseBody
    @RequestMapping(value = "/project/team", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeam(Integer projectId) {

        // 必须传入projectId
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        Map<String, Object> allTeamMembersInProject = projectTeamService.getAllTeamMembersInProject(projectId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, allTeamMembersInProject);
    }

    //添加项目团队成员，必须持有权限
    @RequiresPermissions(value = {PermissionStrings.PROJECT_TEAM_ADD})
    @ResponseBody
    @RequestMapping(value = "/project/team_join", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamAdd(ProjectTeam projectTeam) {

        //projectId以及userId不能为空
        if (projectTeam.getProjectId() == null || projectTeam.getUserId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.USER_ID_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        if (projectTeamService.isInProject(projectTeam.getProjectId(), projectTeam.getUserId())) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.RECORD_REPEAT + "指定成员已参与该项目！", "projectId Or userId Can not be NULL!");
        }

        //如果指定用户并不是监理工程师角色，不可加入到项目
        User engineer = userService.getUser(projectTeam.getUserId());
        if (engineer.getRoleId() != RoleSignal.ROLE_ENGINEER) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.USER_ID_CANNOT_BE_NULL, "userId Can not be NULL!");
        }

        boolean result = projectTeamService.addMemberToProject(projectTeam);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, projectTeam);
    }

    //移出项目团队成员，必须持有权限
    @RequiresPermissions(value = {PermissionStrings.PROJECT_TEAM_REMOVE})
    @ResponseBody
    @RequestMapping(value = "/project/team_quit", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamQuit(Integer projectId, Integer userId) {

        //projectId以及userId不能为空
        if (projectId == null || userId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.USER_ID_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        if (userService.getUser(userId).getRoleId().intValue() > 1) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "无法移出总监代表以上人员！", null);
        }

        boolean result = projectTeamService.removeMemberFromProject(projectId, userId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 更改项目总监
    @RequiresPermissions(value = {PermissionStrings.PROJECT_TEAM_CHANGE_DIRECTOR})
    @ResponseBody
    @RequestMapping(value = "/project/team_director_change", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamDirectorChange(Integer projectId, Integer userId) {

        //projectId以及userId不能为空
        if (projectId == null || userId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.USER_ID_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        User director = userService.getUser(userId);
        if (director.getRoleId() != RoleSignal.ROLE_DIRECTOR) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.USER_ID_CANNOT_BE_NULL, "userId Can not be NULL!");
        }

        short role = 2;  //2表示项目总监
        boolean result = projectTeamService.changeDirectorOrDelegate(projectId, userId, role);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 更改总监代表
    @RequiresPermissions(value = {PermissionStrings.PROJECT_TEAM_CHANGE_DELEGATE})
    @ResponseBody
    @RequestMapping(value = "/project/team_delegate_change", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamDelegateChange(Integer projectId, Integer userId) {

        //projectId以及userId不能为空
        if (projectId == null || userId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.USER_ID_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        User delegate = userService.getUser(userId);

        if (delegate.getRoleId() != RoleSignal.ROLE_DELEGATE) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.USER_ID_CANNOT_BE_NULL, "userId Can not be NULL!");
        }

        short role = 1;  //1表示总监代表
        boolean result = projectTeamService.changeDirectorOrDelegate(projectId, userId, role);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 查看用户信息
    @ResponseBody
    @RequestMapping(value = "/project/team_user", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamQueryUser(Integer userId) {

        //userId不能为空
        if (userId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.USER_ID_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        User user = userService.getUser(userId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewUserBuild(user));
    }

    // 查看部门信息
    @ResponseBody
    @RequestMapping(value = "/project/team_user_department", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTeamQueryUserDepartment(Integer departmentId) {

        //departmentId不能为空
        if (departmentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DEPARTMENT_CANNOT_BE_NULL, "projectId Or userId Can not be NULL!");
        }

        Department department = departmentService.getDepartment(departmentId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, department);
    }

    // 组装user视图model
    private ViewUser viewUserBuild(User user) {
        ViewUser viewUser = new ViewUser();
        viewUser.setId(user.getId());
        viewUser.setRoleId(user.getRoleId());
        viewUser.setDepartmentId(user.getDepartmentId());
        viewUser.setDepartment(departmentService.getDepartment(user.getDepartmentId()));
        viewUser.setDepartmentName(departmentService.getDepartment(user.getDepartmentId()).getDepartment());
        viewUser.setWorkCode(user.getWorkCode());
        viewUser.setPassword("");
        viewUser.setMobile(user.getMobile());
        viewUser.setUserState(user.getUserState());
        viewUser.setUserRealname(user.getUserRealname());
        return viewUser;
    }

}
