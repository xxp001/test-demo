package com.supervision.project.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import com.supervision.project.ViewModel.*;
import com.supervision.project.model.*;
import com.supervision.project.service.ProjectCostService;
import com.supervision.project.service.ProjectService;
import com.supervision.project.service.ProjectTeamService;
import com.supervision.user.model.Role;
import com.supervision.user.model.User;
import com.supervision.user.service.RoleService;
import com.supervision.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
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

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:project cost controller
 * @Author:TjSanshao
 * @Create:2019-02-08 12:28
 *
 **/
@Controller
@CrossOrigin
@RequiresAuthentication
public class ProjectCostController {

    @Autowired
    private ProjectCostService projectCostService;

    @Autowired
    private ProjectTeamService projectTeamService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    // 查看一个项目的成本列表
    @ResponseBody
    @RequestMapping(value = "/project/cost_list", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectCost(Integer projectId) {

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

        List<HumanCostRecord> allHumanCostRecord = projectCostService.getAllHumanCostRecord(projectId);
        List<ExtraCost> allExtraCost = projectCostService.getAllExtraCost(projectId);

        Map<String, Object> result = new HashMap<>();
        result.put("HumanCostRecord", this.viewHumanCostRecordListBuild(allHumanCostRecord));
        result.put("ExtraCost", allExtraCost);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, result);
    }

    // 查看一个阶段的成本列表
    @ResponseBody
    @RequestMapping(value = "/project/moment_cost_list", method = {RequestMethod.GET, RequestMethod.POST})
    public String momentCost(Integer momentId) {

        if (momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "momentId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectService.getMoment(momentId).getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        List<HumanCostRecord> allHumanCostRecord = projectCostService.getAllHumanCostRecordByMoment(momentId);
        List<ExtraCost> allExtraCost = projectCostService.getAllExtraCostByMoment(momentId);

        Map<String, Object> result = new HashMap<>();
        result.put("HumanCostRecord", this.viewHumanCostRecordListBuild(allHumanCostRecord));
        result.put("ExtraCost", allExtraCost);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, result);
    }

    // 查看成本，该接口会被弃用
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/project/cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectAndMomentCost(Integer projectId) {

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

        ProjectCost projectCost = projectCostService.getProjectCost(projectId);
        List<MomentCost> momentCosts = projectCostService.getMomentCosts(projectId);

        List<ViewMomentCost> viewMomentCosts = new ArrayList<>();

        for (int i = 0; i < momentCosts.size(); i++) {
            MomentCost tempCost = momentCosts.get(i);

            ViewMomentCost viewMomentCost = new ViewMomentCost();
            viewMomentCost.setId(tempCost.getId());
            viewMomentCost.setProjectId(tempCost.getProjectId());
            viewMomentCost.setMomentId(tempCost.getMomentId());
            viewMomentCost.setExpectCost(tempCost.getExpectCost());
            viewMomentCost.setRealCost(tempCost.getRealCost());
            viewMomentCost.setMomentName(projectService.getMoment(tempCost.getMomentId()).getMomentName());

            viewMomentCosts.add(viewMomentCost);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("projectCost", projectCost);
        result.put("momentCosts", viewMomentCosts);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, result);
    }

    // 查看成员成本，该接口可能会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/member_cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectCostByMembers(Integer projectId) {
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
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
        User projectManager = (User)allTeamMembersInProject.get("ProjectManager");
        User delegate = (User)allTeamMembersInProject.get("Delegate");

        List<ViewProjectMemberCost> membersCost = new LinkedList<>();
        List<User> teams = (List<User>) allTeamMembersInProject.get("Members");
        teams.add(projectManager);
        teams.add(delegate);
        for (int i = 0; i < teams.size(); i++) {
            membersCost.add(this.viewProjectMemberCostBuild(projectId, teams.get(i)));
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, membersCost);
    }

    // 查看登录用户自己的成本列表，按照时间分组
    @ResponseBody
    @RequestMapping(value = "/project/person_cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectCostPerson(Integer projectId, Integer page, Integer pageSize) {
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
//        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
//            // 没有参与项目
//            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
//        }

        // 执行到这里，证明登录的用户参与了项目，可以查看自己提交的记录

//        // 测试使用
//        User userInLogin = userService.getUser(129);

        PageInfo<HumanCostRecord> humanCostRecordPageInfo = projectCostService.listHumanCostRecordByUserByPage(projectId, userInLogin.getId(), page, pageSize);

        // 获取到分页信息里面的对象列表
        List<HumanCostRecord> humanCostRecords = humanCostRecordPageInfo.getList();

        // 没有上传过工作记录
        if (humanCostRecords.size() == 0) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, new ArrayList<ViewCostByDate>());
        }

        // 视图组装完成
        List<ViewCostByDate> viewCostByDates = this.viewCostByDatesBuild(humanCostRecords);

        // 开始组装返回数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", humanCostRecordPageInfo.getTotal());
        resultMap.put("pageNum", humanCostRecordPageInfo.getPageNum());
        resultMap.put("pageSize", humanCostRecordPageInfo.getPageSize());
        resultMap.put("pages", humanCostRecordPageInfo.getPages());
        resultMap.put("list", viewCostByDates);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看登录用户自己的成本列表（根据阶段），按照时间分组
    @ResponseBody
    @RequestMapping(value = "/project/moment_person_cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String momentCostPerson(Integer momentId, Integer page, Integer pageSize) {
        if (momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, null);
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectService.getMoment(momentId).getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
        }

        //执行到这里，证明登录的用户参与了项目，可以查看自己提交的记录

        // 测试使用
//        User userInLogin = userService.getUser(129);

        PageInfo<HumanCostRecord> humanCostRecordPageInfo = projectCostService.listHumanCostRecordByUserInMomentByPage(momentId, userInLogin.getId(), page, pageSize);

        // 获取到分页信息里面的对象列表
        List<HumanCostRecord> humanCostRecords = humanCostRecordPageInfo.getList();

        // 没有上传过工作记录
        if (humanCostRecords.size() == 0) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, new ArrayList<ViewCostByDate>());
        }

        List<ViewCostByDate> viewCostByDates = this.viewCostByDatesBuild(humanCostRecords);

        // 开始组装返回数据
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", humanCostRecordPageInfo.getTotal());
        resultMap.put("pageNum", humanCostRecordPageInfo.getPageNum());
        resultMap.put("pageSize", humanCostRecordPageInfo.getPageSize());
        resultMap.put("pages", humanCostRecordPageInfo.getPages());
        resultMap.put("list", viewCostByDates);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看项目成员在整个项目目前的成本，需要权限
    @RequiresPermissions(value = {PermissionStrings.PROJECT_READ_ALL})
    @ResponseBody
    @RequestMapping(value = "/project/member_total_cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTotalCostByMember(Integer projectId) {
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
        }

        // 确定project存在，如果存在，必定存在projectTeam
        if (projectService.getProject(projectId) == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
        }

        Map<String, Object> allTeamMembersInProject = projectTeamService.getAllTeamMembersInProject(projectId);

        List<User> projectTeam = new LinkedList<>();

        List<User> members = (List<User>) allTeamMembersInProject.get("Members");
        User projectManager = (User) allTeamMembersInProject.get("ProjectManager");
        User delegate = (User) allTeamMembersInProject.get("Delegate");

        projectTeam.add(projectManager);
        projectTeam.add(delegate);
        projectTeam.addAll(members);

        List<ViewProjectCostAmount> viewProjectCostAmounts = this.viewProjectCostAmountsBuild(projectTeam, projectId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewProjectCostAmounts);
    }

    // 查看成员在每个阶段的成本
    @ResponseBody
    @RequestMapping(value = "/project/member_cost_project")
    public String momentCostByMembersWholeProject(Integer projectId) {
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
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
        User projectManager = (User)allTeamMembersInProject.get("ProjectManager");
        User delegate = (User)allTeamMembersInProject.get("Delegate");

        List<Moment> allMoments = projectService.getAllMoments(projectId);

        List<ViewMomentMemberCost> membersMomentCost = new LinkedList<>();
        List<User> teams = (List<User>) allTeamMembersInProject.get("Members");
        teams.add(projectManager);
        teams.add(delegate);
        for (int i = 0; i < allMoments.size(); i++) {
            int momentId = allMoments.get(i).getId();
            for (int j = 0; j < teams.size(); j++) {
                membersMomentCost.add(viewMomentMemberCostBuild(projectId, momentId, teams.get(j).getId()));
            }
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, membersMomentCost);
    }

    // 查看成员在指定阶段的成本
    @ResponseBody
    @RequestMapping(value = "/project/moment_member_cost")
    public String momentCostByMembers(Integer projectId, Integer momentId) {
        if (projectId == null || momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
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
        User projectManager = (User)allTeamMembersInProject.get("ProjectManager");
        User delegate = (User)allTeamMembersInProject.get("Delegate");

        List<ViewMomentMemberCost> membersMomentCost = new LinkedList<>();
        List<User> teams = (List<User>) allTeamMembersInProject.get("Members");
        teams.add(projectManager);
        teams.add(delegate);
        for (int i = 0; i < teams.size(); i++) {
            membersMomentCost.add(viewMomentMemberCostBuild(projectId, momentId, teams.get(i).getId()));
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, membersMomentCost);
    }

    // 录入成本
    @ResponseBody
    @RequestMapping(value = "/project/cost_build")
    public String projectAddHumanCostAndExtraCost(ViewCost viewCost) {

        Integer projectId = viewCost.getProjectId();
        Integer momentIdTemp = viewCost.getMomentId();

        if (projectId == null || momentIdTemp == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, null);
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
            // 没有参与项目
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "不是该项目成员，不可操作！", null);
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        //设置上传用户
//        User userInLogin = userService.getUser(129); // 测试使用
        viewCost.setUploadFrom(userInLogin.getId());

        Moment moment = null;
        //这里确保momentId填充到数据库
        List<Moment> projectMomentsRunning = projectService.getProjectMomentRunning(projectId);
        if (projectMomentsRunning.size() == 0) {
            moment = new Moment();
            moment.setId(projectService.getAllMoments(projectId).get(0).getId());
        }
        //如果用户没有上传momentId，那么将使用正在进行的第一个项目阶段或者项目的第一个项目阶段（如果没有阶段正在进行的话）
        Integer momentId = momentIdTemp == null ? moment.getId() : momentIdTemp;

        boolean result = projectCostService.addCost(viewCost);
        if (!result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "今天已录入成本！如有问题请联系管理员！", viewCost);
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewCost);
    }

    // 录入补贴的成本，接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/extra_cost_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectExtraCostAdd(ExtraCost extraCost) {

        if (extraCost.getProjectId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(extraCost.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        //设置上传时间
        extraCost.setUploadTime(new Date());

        //设置上传用户
        extraCost.setUploadFrom(userInLogin.getId());

        //设置momentId
        Moment projectMomentRunning = projectService.getProjectMomentRunning(extraCost.getProjectId()).get(0);
        if (projectMomentRunning == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "No Moment Is Running!", extraCost);
        }
        extraCost.setMomentId(projectMomentRunning.getId());

        short state = 0;
        extraCost.setApplyState(state);

        boolean result = projectCostService.addExtraCost(extraCost);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, extraCost);
    }

    // 接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/extra_cost_apply", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectExtraCostThrough(Integer extraCostId) {

        if (extraCostId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "This extraCost is not available!");
        }

        boolean result = projectCostService.throughExtraCost(extraCostId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/extra_cost_against", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectExtraCostAgainst(Integer extraCostId) {

        if (extraCostId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "This extraCost is not available!");
        }

        boolean result = projectCostService.againstExtraCost(extraCostId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 上传工时记录，接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/work_record_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectHumanCostRecordAdd(HumanCostRecord record) {

        if (record.getProjectId() == null || record.getMomentId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Or momentId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(record.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        record.setUploadTime(new Date());

        record.setUploadFrom(userInLogin.getId());

        //设置momentId
        Moment projectMomentRunning = projectService.getProjectMomentRunning(record.getProjectId()).get(0);
        if (projectMomentRunning == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "No Moment Is Running!", record);
        }
        record.setMomentId(projectMomentRunning.getId());

        short state = 0;
        record.setApplyState(state);

        boolean result = projectCostService.addHumanCostRecord(record);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, record);
    }

    // 接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/work_record_apply", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectHumanCostRecordThrough(Integer humanCostRecordId) {

        if (humanCostRecordId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "humanCostRecordId Can not be NULL!");
        }

        boolean result = projectCostService.throughHumanCost(humanCostRecordId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 接口会被弃用
    @ResponseBody
    @RequestMapping(value = "/project/work_record_against", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectHumanCostRecordAgainst(Integer humanCostRecordId) {

        if (humanCostRecordId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "humanCostRecordId Can not be NULL!");
        }

        boolean result = projectCostService.againstHumanCost(humanCostRecordId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    // 查看所有角色成本
    @RequiresPermissions(value = {PermissionStrings.PROJECT_ROLECOST_MANAGE})
    @ResponseBody
    @RequestMapping(value = "/project/role_cost_list", method = {RequestMethod.GET, RequestMethod.POST})
    public String roleCosts() {
        List<RoleCost> allRoleCost = projectCostService.getAllRoleCost();
        List<ViewRoleCost> allViewRoles = new ArrayList<>();
        for (int i = 0; i < allRoleCost.size(); i++) {
            RoleCost roleCostTemp = allRoleCost.get(i);

            ViewRoleCost viewRoleCost = new ViewRoleCost();
            viewRoleCost.setId(roleCostTemp.getRoleId());
            viewRoleCost.setRoleCostHalfDay(roleCostTemp.getRoleCostHalfDay());
            viewRoleCost.setRoleCostDesc(roleCostTemp.getRoleCostDesc());

            allViewRoles.add(viewRoleCost);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, allViewRoles);
    }

    // 获取登录用户的半天工时薪资
    @ResponseBody
    @RequestMapping(value = "/project/role_cost", method = {RequestMethod.GET, RequestMethod.POST})
    public String roleCost() {

        Integer userId = ((User) SecurityUtils.getSubject().getSession().getAttribute(StaticProperties.SESSION_USER)).getId();

        //从数据库查询RoleCost
        RoleCost roleCostByUser = projectCostService.getRoleCostByUser(userId);

        //构建视图RoleCost
        ViewRoleCost viewRoleCost = new ViewRoleCost();
        viewRoleCost.setId(roleCostByUser.getRoleId());
        viewRoleCost.setRoleCostHalfDay(roleCostByUser.getRoleCostHalfDay());
        viewRoleCost.setRoleCostDesc(roleCostByUser.getRoleCostDesc());

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewRoleCost);
    }

    // 查看所有角色成本
    @RequiresPermissions(value = {PermissionStrings.PROJECT_ROLECOST_MANAGE})
    @ResponseBody
    @RequestMapping(value = "/project/role_costs", method = {RequestMethod.GET, RequestMethod.POST})
    public String allRoleCost() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewRoleCostsBuild(projectCostService.getAllRoleCost()));
    }

    // 增加角色成本，属于设置
    @RequiresPermissions(value = {PermissionStrings.PROJECT_ROLECOST_MANAGE})
    @ResponseBody
    @RequestMapping(value = "/project/role_cost_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String roleCostsAdd(RoleCost roleCost) {
        if (projectCostService.isExistRoleCost(roleCost.getRoleId())) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.RECORD_REPEAT, "Already Exists!");
        }

        roleCost.setRoleCostDesc(projectCostService.getRoleById(roleCost.getRoleId()).getRoledesc());

        boolean result = projectCostService.addRoleCost(roleCost);

        if (!result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, roleCost);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, roleCost);
    }

    // 修改指定角色的成本
    @RequiresPermissions(value = {PermissionStrings.PROJECT_ROLECOST_MANAGE})
    @ResponseBody
    @RequestMapping(value = "/project/role_cost_modify", method = {RequestMethod.GET, RequestMethod.POST})
    public String roleCostsUpdate(RoleCost roleCost) {
        if (!projectCostService.isExistRoleCost(roleCost.getRoleId())) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "Not Exists!");
        }

        boolean result = projectCostService.updateRoleCost(roleCost);

        if (!result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, roleCost);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, roleCost);
    }

    private ViewProjectMemberCost viewProjectMemberCostBuild(ProjectTeam memberInProject) {
        ViewProjectMemberCost projectMemberCost = new ViewProjectMemberCost();

        projectMemberCost.setMember(userService.getUser(memberInProject.getUserId()));

        RoleCost roleCostByUser = projectCostService.getRoleCostByUser(memberInProject.getUserId());
        projectMemberCost.setCostHalfDay(roleCostByUser.getRoleCostHalfDay());

        Short roleInProject = memberInProject.getRoleInProject();
        if (roleInProject.intValue() == 0) {
            projectMemberCost.setRole("监理工程师");
        }
        if (roleInProject.intValue() == 1) {
            projectMemberCost.setRole("总监代表");
        }
        if (roleInProject.intValue() == 2) {
            projectMemberCost.setRole("项目总监");
        }

        //为了数据简洁，不将数据封装到对象中
        //projectMemberCost.setProject(projectService.getProject(memberInProject.getProjectId()));

        //在项目中的成本
        BigDecimal costInProject = new BigDecimal(0);

        List<HumanCostRecord> allHumanCostRecord = projectCostService.getAllHumanCostRecord(memberInProject.getProjectId());
        List<HumanCostRecord> humanCostRecordsByUser = new LinkedList<>();
        //懒得写sql语句了，用for循环了
        for (int i = 0; i < allHumanCostRecord.size(); i++) {
            HumanCostRecord humanCostRecord = allHumanCostRecord.get(i);
            //如果上传人是该用户
            if (humanCostRecord.getUploadFrom() == memberInProject.getUserId()) {
                humanCostRecordsByUser.add(humanCostRecord);
                costInProject = costInProject.add(new BigDecimal(humanCostRecord.getWorkTime()).multiply(projectMemberCost.getCostHalfDay()));
            }
        }
        //为了数据简洁，不将列表封装到对象中
        //projectMemberCost.setHumanCostRecords(humanCostRecordsByUser);

        List<ExtraCost> allExtraCost = projectCostService.getAllExtraCost(memberInProject.getProjectId());
        List<ExtraCost> extraCostsByUser = new LinkedList<>();
        for (int i = 0; i < allExtraCost.size(); i++) {
            ExtraCost extraCost = allExtraCost.get(i);
            if (extraCost.getUploadFrom() == memberInProject.getUserId()) {
                extraCostsByUser.add(extraCost);
                costInProject = costInProject.add(extraCost.getAmount());
            }
        }
        //为了数据简洁，不将列表封装到对象中
        //projectMemberCost.setExtraCosts(extraCostsByUser);

        projectMemberCost.setCostInProject(costInProject);

        return projectMemberCost;
    }

    private ViewProjectMemberCost viewProjectMemberCostBuild(Integer projectId, User member) {
        ProjectTeam byProjectAndUser = projectTeamService.getByProjectAndUser(projectId, member.getId());
        return this.viewProjectMemberCostBuild(byProjectAndUser);
    }

    private ViewMomentMemberCost viewMomentMemberCostBuild(Integer projectId, Integer momentId, Integer userId) {
        ViewMomentMemberCost viewMomentMemberCost = new ViewMomentMemberCost();

        User member = userService.getUser(userId);

        viewMomentMemberCost.setMember(member);

        RoleCost roleCostByUser = projectCostService.getRoleCostByUser(member.getId());
        viewMomentMemberCost.setCostHalfDay(roleCostByUser.getRoleCostHalfDay());

        ProjectTeam memberInProject = projectTeamService.getByProjectAndUser(projectId, member.getId());
        Short roleInProject = memberInProject.getRoleInProject();
        if (roleInProject.intValue() == 0) {
            viewMomentMemberCost.setRole("监理工程师");
        }
        if (roleInProject.intValue() == 1) {
            viewMomentMemberCost.setRole("总监代表");
        }
        if (roleInProject.intValue() == 2) {
            viewMomentMemberCost.setRole("项目总监");
        }

        viewMomentMemberCost.setMoment(projectService.getMoment(momentId));

        BigDecimal costInMoment = new BigDecimal(0);

        List<HumanCostRecord> allHumanCostRecord = projectCostService.getAllHumanCostRecordByMoment(momentId);
        List<HumanCostRecord> humanCostRecordsByUser = new LinkedList<>();
        //懒得写sql语句了，用for循环了
        for (int i = 0; i < allHumanCostRecord.size(); i++) {
            HumanCostRecord humanCostRecord = allHumanCostRecord.get(i);
            //如果上传人是该用户
            if (humanCostRecord.getUploadFrom() == memberInProject.getUserId()) {
                humanCostRecordsByUser.add(humanCostRecord);
                costInMoment = costInMoment.add(new BigDecimal(humanCostRecord.getWorkTime()).multiply(viewMomentMemberCost.getCostHalfDay()));
            }
        }

        List<ExtraCost> allExtraCost = projectCostService.getAllExtraCostByMoment(momentId);
        List<ExtraCost> extraCostsByUser = new LinkedList<>();
        for (int i = 0; i < allExtraCost.size(); i++) {
            ExtraCost extraCost = allExtraCost.get(i);
            if (extraCost.getUploadFrom() == memberInProject.getUserId()) {
                extraCostsByUser.add(extraCost);
                costInMoment = costInMoment.add(extraCost.getAmount());
            }
        }

        viewMomentMemberCost.setCostInMoment(costInMoment);

        return viewMomentMemberCost;
    }

    private ViewHumanCostRecord viewHumanCostRecordBuild(HumanCostRecord humanCostRecord) {
        ViewHumanCostRecord record = new ViewHumanCostRecord();
        record.setId(humanCostRecord.getId());
        record.setProjectId(humanCostRecord.getProjectId());
        record.setMomentId(humanCostRecord.getMomentId());
        record.setUploadFrom(humanCostRecord.getUploadFrom());
        record.setUploadTime(humanCostRecord.getUploadTime());
        record.setWorkTime(humanCostRecord.getWorkTime());
        record.setApplyState(humanCostRecord.getApplyState());
        record.setWorkCode(humanCostRecord.getWorkCode());
        record.setUploaderName(humanCostRecord.getUploaderName());

        RoleCost roleCostByUser = projectCostService.getRoleCostByUser(humanCostRecord.getUploadFrom());
        record.setCostHalfDay(roleCostByUser.getRoleCostHalfDay());
        record.setHumanCost(roleCostByUser.getRoleCostHalfDay().multiply(new BigDecimal(humanCostRecord.getWorkTime())));

        return record;
    }

    private List<ViewHumanCostRecord> viewHumanCostRecordListBuild(List<HumanCostRecord> humanCostRecordList) {
        List<ViewHumanCostRecord> list = new LinkedList<>();
        for (int i = 0; i < humanCostRecordList.size(); i++) {
            list.add(this.viewHumanCostRecordBuild(humanCostRecordList.get(i)));
        }
        return list;
    }

    private List<ViewCostByDate> viewCostByDatesBuild(List<HumanCostRecord> humanCostRecords) {
        List<ViewCostByDate> viewCostByDates = new LinkedList<>();

        // 遍历humanCostRecord，如果extraCost的uploadTime和humanCostRecord的uploadTime相等，那么将是同一天上传的，封装为一个对象
        for (int i = 0; i < humanCostRecords.size(); i++) {
            HumanCostRecord tempRecord = humanCostRecords.get(i);
            ViewCostByDate viewCostByDate = new ViewCostByDate();
            LinkedList<ExtraCost> tempExtraCosts = new LinkedList<>();
            viewCostByDate.setProjectId(tempRecord.getProjectId());
            viewCostByDate.setMomentId(tempRecord.getMomentId());
            viewCostByDate.setUploadFrom(tempRecord.getUploadFrom());
            viewCostByDate.setUploaderName(tempRecord.getUploaderName());
            viewCostByDate.setWorkCode(tempRecord.getWorkCode());
            Short roleInProject = projectTeamService.getByProjectAndUser(viewCostByDate.getProjectId(), viewCostByDate.getUploadFrom()).getRoleInProject();
            viewCostByDate.setRoleInProject(roleInProject.intValue());
            if (roleInProject.intValue() == 0) {
                viewCostByDate.setRole("监理工程师");
            } else if (roleInProject.intValue() == 1) {
                viewCostByDate.setRole("总监代表");
            } else if (roleInProject.intValue() == 2) {
                viewCostByDate.setRole("项目总监");
            }
            viewCostByDate.setUploadedTimeObject(tempRecord.getUploadTime());
            viewCostByDate.setUploadedTime(this.parseDate(viewCostByDate.getUploadedTimeObject()));
            viewCostByDate.setWorkTime(tempRecord.getWorkTime().intValue());
            viewCostByDate.setCostHalfDay(projectCostService.getRoleCostByUser(viewCostByDate.getUploadFrom()).getRoleCostHalfDay());
            viewCostByDate.setHumanCostAmount(viewCostByDate.getCostHalfDay().multiply(new BigDecimal(viewCostByDate.getWorkTime())));
            viewCostByDate.setHumanCostRecord(tempRecord);

            // 这里不管使用查找同一个moment同一个时间上传的，其实查找同一个project同一个时间的也可以
            List<ExtraCost> extraCosts = projectCostService.listExtraCostByUserByTimeInMoment(tempRecord.getMomentId(), tempRecord.getUploadFrom(), tempRecord.getUploadTime());

            for (ExtraCost ex : extraCosts) {
                tempExtraCosts.add(ex);
            }

            viewCostByDate.setExtraCosts(tempExtraCosts);
            viewCostByDates.add(viewCostByDate);
        }
        return viewCostByDates;
    }

    private String parseDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    private List<ViewProjectCostAmount> viewProjectCostAmountsBuild(List<User> users, Integer projectId) {
        List<ViewProjectCostAmount> list = new LinkedList<>();

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            ViewProjectCostAmount viewProjectCostAmount = new ViewProjectCostAmount();
            viewProjectCostAmount.setUserId(user.getId());
            viewProjectCostAmount.setProjectId(projectId);
            viewProjectCostAmount.setRealName(user.getUserRealname());
            viewProjectCostAmount.setRole(roleService.getRoleById(user.getRoleId()).getRoledesc());

            viewProjectCostAmount.setHumanCost(new BigDecimal(0));  // 人力
            viewProjectCostAmount.setStay(new BigDecimal(0));  // 差旅
            viewProjectCostAmount.setMeals(new BigDecimal(0));  // 伙食
            viewProjectCostAmount.setTraffic(new BigDecimal(0));  // 交通
            viewProjectCostAmount.setOthers(new BigDecimal(0));  // 其他

            // 查找用户在该项目所有的extraCost和humanCostRecord
            List<ExtraCost> extraCosts = projectCostService.listExtraCostByUser(projectId, user.getId());
            List<HumanCostRecord> humanCostRecords = projectCostService.listHumanCostRecordByUser(projectId, user.getId());

            BigDecimal roleCostHalfDay = projectCostService.getRoleCostByUser(user.getId()).getRoleCostHalfDay();

            for (HumanCostRecord humanCostRecord : humanCostRecords) {
                BigDecimal humanCostByUser = new BigDecimal(humanCostRecord.getWorkTime().intValue()).multiply(roleCostHalfDay);
                viewProjectCostAmount.setHumanCost(viewProjectCostAmount.getHumanCost().add(humanCostByUser));
            }

            for (ExtraCost extraCost : extraCosts) {
                String costDesc = extraCost.getCostDesc();
                BigDecimal amount = extraCost.getAmount();
                if (costDesc.equals("差旅")) {
                    viewProjectCostAmount.setStay(viewProjectCostAmount.getStay().add(amount));
                } else if (costDesc.equals("伙食")) {
                    viewProjectCostAmount.setMeals(viewProjectCostAmount.getMeals().add(amount));
                } else if (costDesc.equals("交通")) {
                    viewProjectCostAmount.setTraffic(viewProjectCostAmount.getTraffic().add(amount));
                } else {
                    // 其他
                    viewProjectCostAmount.setOthers(viewProjectCostAmount.getOthers().add(amount));
                }
            }

            // 到这里，所有的成本都加入到了对象中

            list.add(viewProjectCostAmount);
        }

        return list;
    }

    private List<ViewRoleCost> viewRoleCostsBuild(List<RoleCost> roleCosts) {
        List<ViewRoleCost> list = new LinkedList<>();
        for (int i = 0; i < roleCosts.size(); i++) {
            RoleCost roleCost = roleCosts.get(i);
            ViewRoleCost viewRoleCost = new ViewRoleCost();
            viewRoleCost.setId(roleCost.getRoleId());
            viewRoleCost.setRole(projectCostService.getRoleById(roleCost.getRoleId()));
            viewRoleCost.setRoleCostHalfDay(roleCost.getRoleCostHalfDay());
            viewRoleCost.setRoleName(roleCost.getRoleCostDesc());
            list.add(viewRoleCost);
        }
        return list;
    }
}
