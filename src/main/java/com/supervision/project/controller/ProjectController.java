package com.supervision.project.controller;

import com.github.pagehelper.PageInfo;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import com.supervision.document.ViewModel.ViewDocumentByType;
import com.supervision.document.model.Document;
import com.supervision.document.service.DocumentService;
import com.supervision.project.ViewModel.ViewDailyAndHumanCost;
import com.supervision.project.ViewModel.ViewMoment;
import com.supervision.project.ViewModel.ViewProject;
import com.supervision.project.ViewModel.ViewUser;
import com.supervision.project.model.*;
import com.supervision.project.service.ProjectCostService;
import com.supervision.project.service.ProjectService;
import com.supervision.project.service.ProjectTeamService;
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

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:project controller
 * @Author:TjSanshao
 * @Create:2019-02-03 15:18
 *
 **/
@Controller
@CrossOrigin
@RequiresAuthentication
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ProjectCostService projectCostService;

    @Autowired
    private ProjectTeamService projectTeamService;

    //创建项目
    @RequiresPermissions(value = {PermissionStrings.PROJECT_BUILD})
    @ResponseBody
    @RequestMapping(value = "/project/build", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectAdd(Project project, HttpSession session) {

        //项目名称不能为空
        if (project.getProjectName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_NAME_CANNOT_BE_NULL, project);
        }

        //项目代码不能为空
        if (project.getProjectCode() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_CODE_CANNOT_BE_NULL, project);
        }

        //项目类型不能为空
        if (project.getProjectType() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_TYPE_CANNOT_BE_NULL, project);
        }
        if (project.getProjectType() == 2) {
            // 2表示是自定义类型
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.REQUEST_ERROR, "Please Use Another Request!");
        }

        //项目总监不能为空
        if (project.getProjectDirector() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_DIRECTOR_CANNOT_BE_NULL, project);
        }

        //总监代表不能为空
        if (project.getDelegate() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_DELEGATE_CANNOT_BE_NULL, project);
        }

        //部门不能为空
        if (project.getDepartment() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DEPARTMENT_CANNOT_BE_NULL, project);
        }

        //如果没有填写项目描述
        if (project.getProjectDesc() == null) {
            project.setProjectDesc("该项目没有详细描述！");
        }

        //项目的预计完成时间，这里不是必填，但还没想好怎么给默认值
        if (project.getExpectFinishTime() == null) {
            project.setExpectFinishTime(new Date());
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();

        //设置创建人，根据登录的用户
        project.setCreateFrom(((User)currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER)).getId());

        projectService.addProject(project);

        ViewProject viewProject = viewProjectBuild(project);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewProject);
    }

    //创建自定义项目
    @RequiresPermissions(value = {PermissionStrings.PROJECT_BUILD})
    @ResponseBody
    @RequestMapping(value = "/project/build_custom", method = {RequestMethod.GET, RequestMethod.POST})
    public String customProjectAdd(CustomProject project) {

        //项目名称不能为空
        if (project.getProjectName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_NAME_CANNOT_BE_NULL, project);
        }

        //项目代码不能为空
        if (project.getProjectCode() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_CODE_CANNOT_BE_NULL, project);
        }

        //项目类型不能为空
        if (project.getProjectType() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_TYPE_CANNOT_BE_NULL, project);
        }
        if (project.getProjectType() != 2) {
            // !=2表示不是自定义类型
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.REQUEST_ERROR, "Please Use Another Request!");
        }

        //项目总监不能为空
        if (project.getProjectDirector() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_DIRECTOR_CANNOT_BE_NULL, project);
        }

        //总监代表不能为空
        if (project.getDelegate() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_DELEGATE_CANNOT_BE_NULL, project);
        }

        //部门不能为空
        if (project.getDepartment() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DEPARTMENT_CANNOT_BE_NULL, project);
        }

        //如果没有填写项目描述
        if (project.getProjectDesc() == null) {
            project.setProjectDesc("该项目没有详细描述");
        }

        //项目的预计完成时间，这里不是必填，但还没想好怎么给默认值
        if (project.getExpectFinishTime() == null) {
            project.setExpectFinishTime(new Date());
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();

        //设置创建人，根据登录的用户
        project.setCreateFrom(((User)currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER)).getId());

        projectService.addCustomProject(project);

        //将用户临时设定为项目总监和总监代表，拥有高一级的权限
        //这里应该在service中使用事务来控制，懒得改
        userService.setUserDirector(project.getProjectDirector());
        userService.setUserDelegate(project.getDelegate());

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, project);
    }

    // 创建项目阶段
    @RequiresPermissions(value = {PermissionStrings.PROJECT_MOMENT_BUILD})
    @ResponseBody
    @RequestMapping(value = "/project/moment_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String customMomentAdd(CustomMoment moment) {
        if (moment.getProjectId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, null);
        }
        if (moment.getMomentName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "项目阶段名不能为空！", null);
        }

        if (moment.getMomentDesc() == null) {
            // 如果desc为空，默认为项目阶段名
            moment.setMomentDesc(moment.getMomentName());
        }

        if (moment.getExpectFinishTime() == null) {
            // 如果预计完成时间为空
            moment.setExpectFinishTime(new Date());
        }

        if (!projectService.addCustomMoment(moment)) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
    }

    //提交工作日志
    @ResponseBody
    @RequestMapping(value = "/project/work_record", method = {RequestMethod.GET, RequestMethod.POST})
    public String dailyAndHumanRecordBuild(ViewDailyAndHumanCost viewDailyAndHumanCost) {

        System.out.println(viewDailyAndHumanCost);

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();

        //获取到登录的用户
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        projectService.addDailyAndHumanCost(viewDailyAndHumanCost, userInLogin.getId());

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewDailyAndHumanCost);
    }

    //获取项目类型
    @ResponseBody
    @RequestMapping(value = "/project/types_list", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectTypes() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, projectService.getAllProjectTypes());
    }

    //管理员可以看到的项目概览，即可以看到所有项目
    @RequiresPermissions(value = {PermissionStrings.PROJECT_READ_ALL})
    @ResponseBody
    @RequestMapping(value = "/project/summary", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectSummary() {
        //这里为了开发方便，直接调用分页接口，使用1000000作为每页记录数，在正常情况下，够用了
        PageInfo<Project> projectPageInfo = (PageInfo<Project>) projectService.getAllProjects(1, 1000000);
        List<Project> projects = projectPageInfo.getList();

        Integer total = projects.size();
        Integer preStart = 0;  //未开始
        Integer running = 0;  //正在进行
        Integer finished = 0;  //已完成

        //减少数据库访问次数
        for (int i = 0; i < projects.size(); i++) {
            int state = projects.get(i).getProjectState().intValue();
            if (state == 0) {
                preStart++;
            }
            if (state == 1) {
                running++;
            }
            if (state == 3) {  //3才是表示已完成
                finished++;
            }
        }

        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("total", total);
        resultMap.put("preStart", preStart);
        resultMap.put("running", running);
        resultMap.put("finished", finished);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //普通用户可以看到的项目概览
    @ResponseBody
    @RequestMapping(value = "/project/summary_normal", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectSummaryNormal() {
        // 获取用户Id
        Session session = SecurityUtils.getSubject().getSession();
        Integer userId = ((User) session.getAttribute(StaticProperties.SESSION_USER)).getId();

        //这里为了开发方便，直接调用分页接口，使用100000作为每页记录数，在正常情况下，够用了
        HashMap<String, Object> allProjectsByUser = (HashMap<String, Object>) projectService.getAllProjectsByUser(1, 100000, userId);
        List<Project> projects = (List<Project>) allProjectsByUser.get("list");

        List<Project> runningProjects = new LinkedList<>();

        Integer total = projects.size();
        Integer preStart = 0;  //未开始
        Integer running = 0;  //正在进行
        Integer finished = 0;  //已完成

        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            int state = project.getProjectState().intValue();
            if (state == 1 || state == 0) {
                runningProjects.add(project);
            }
            if (state == 0) {
                preStart++;
            }
            if (state == 1) {
                running++;
            }
            if (state == 3) {  //3才是表示已完成
                finished++;
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", total);
        resultMap.put("preStart", preStart);
        resultMap.put("running", running);
        resultMap.put("finished", finished);
        resultMap.put("list", runningProjects);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看项目列表
    @ResponseBody
    @RequestMapping(value = "/project/list", method = {RequestMethod.GET, RequestMethod.POST})
    public String allProjects(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Project> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            result = (PageInfo<Project>)projectService.getAllProjects(page, pageSize);
        } else {
            //只能查看自己所在的项目
            result = (PageInfo<Project>)projectService.getAllProjectsByUserWithPageInfo(page, pageSize, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", viewProjectsBuild(result.getList()));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看未开始的项目
    @ResponseBody
    @RequestMapping(value = "/project/list_ready", method = {RequestMethod.GET, RequestMethod.POST})
    public String allProjectsReady(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        short state = 0;  //0表示已准备，未开始

        Map<String, Object> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (Map<String, Object>)projectService.getAllProjectsByState(page, pageSize, state);
        } else {
            //只能查看自己所在的项目
            result = (Map<String, Object>)projectService.getAllProjectsByStateByUser(page, pageSize, state, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.get("total"));
        resultMap.put("pageNum", result.get("pageNum"));
        resultMap.put("pageSize", result.get("pageSize"));
        resultMap.put("pages", result.get("pages"));
        resultMap.put("list", viewProjectsBuild((List<Project>) result.get("list")));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看状态为正在进行的项目列表
    @ResponseBody
    @RequestMapping(value = "/project/list_running", method = {RequestMethod.GET, RequestMethod.POST})
    public String allProjectsRunning(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        short state = 1;  //1表示正在进行

        Map<String, Object> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (Map<String, Object>)projectService.getAllProjectsByState(page, pageSize, state);
        } else {
            //只能查看自己所在的项目
            result = (Map<String, Object>)projectService.getAllProjectsByStateByUser(page, pageSize, state, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.get("total"));
        resultMap.put("pageNum", result.get("pageNum"));
        resultMap.put("pageSize", result.get("pageSize"));
        resultMap.put("pages", result.get("pages"));
        resultMap.put("list", viewProjectsBuild((List<Project>) result.get("list")));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看状态为终验的项目列表
    @ResponseBody
    @RequestMapping(value = "/project/list_final_check", method = {RequestMethod.GET, RequestMethod.POST})
    public String allProjectsFinalCheck(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        short state = 2;  //2表示终验状态

        Map<String, Object> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (Map<String, Object>)projectService.getAllProjectsByState(page, pageSize, state);
        } else {
            //只能查看自己所在的项目
            result = (Map<String, Object>)projectService.getAllProjectsByStateByUser(page, pageSize, state, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.get("total"));
        resultMap.put("pageNum", result.get("pageNum"));
        resultMap.put("pageSize", result.get("pageSize"));
        resultMap.put("pages", result.get("pages"));
        resultMap.put("list", viewProjectsBuild((List<Project>) result.get("list")));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看状态为已完成的项目列表
    @ResponseBody
    @RequestMapping(value = "/project/list_finished", method = {RequestMethod.GET, RequestMethod.POST})
    public String allProjectsFinished(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        short state = 3;  //3表示已完成

        Map<String, Object> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (Map<String, Object>)projectService.getAllProjectsByState(page, pageSize, state);
        } else {
            //只能查看自己所在的项目
            result = (Map<String, Object>)projectService.getAllProjectsByStateByUser(page, pageSize, state, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.get("total"));
        resultMap.put("pageNum", result.get("pageNum"));
        resultMap.put("pageSize", result.get("pageSize"));
        resultMap.put("pages", result.get("pages"));
        resultMap.put("list", viewProjectsBuild((List<Project>) result.get("list")));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //根据时间段查询项目
    @ResponseBody
    @RequestMapping(value = "/project/query_date", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectsDate(Date start, Date end, Integer page, Integer pageSize) {
        if (start == null) {
            start = new Date();
        }
        if (end == null) {
            end = new Date();
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //这里重新确定时间
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        endCalendar.set(Calendar.HOUR, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        end = endCalendar.getTime();

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Project> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (PageInfo<Project>)projectService.getAllProjectsByDate(start, end, page, pageSize);
        } else {
            //只能查看自己所在的项目
            result = (PageInfo<Project>)projectService.getAllProjectsByDate(start, end, page, pageSize, userInLogin.getId());
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", viewProjectsBuild(result.getList()));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //根据关键字查询项目
    @ResponseBody
    @RequestMapping(value = "/project/query", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectsQuery(String query, Integer page, Integer pageSize) {
        if (query == null) {
            query = "";
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        //获取session
        Subject currentUserSubject = SecurityUtils.getSubject();
        Session currentUserSubjectSession = currentUserSubject.getSession();
        User userInLogin = (User) currentUserSubjectSession.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Project> result = null;

        if (currentUserSubject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            //可以查看所有项目
            result = (PageInfo<Project>)projectService.getAllProjectsByQuery(query, page, pageSize);
        } else {
            //只能查看自己所在的项目
            result = (PageInfo<Project>)projectService.getAllProjectsByQueryByUser(query, userInLogin.getId(), page, pageSize);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", viewProjectsBuild(result.getList()));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看项目详情
    @ResponseBody
    @RequestMapping(value = "/project/detail", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectDetail(Integer projectId) {

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

        Project project = projectService.getProject(projectId);

        ViewProject viewProject = viewProjectBuild(project);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewProject);
    }

    //启动项目
    @RequiresPermissions(value = {PermissionStrings.PROJECT_START})
    @ResponseBody
    @RequestMapping(value = "/project/start", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectStart(Integer projectId) {

        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        int stateInt = projectService.getProject(projectId).getProjectState().intValue();

        if (stateInt == 1) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "项目已经处于进行状态，无需再次启动！", null);
        }

        if (stateInt == 2) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_IN_FINAL_CHECK, null);
        }

        if (stateInt == 3) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_FINISHED + "无法进行启动操作！", null);
        }

        //启动项目
        boolean result = projectService.setProjectStart(projectId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    //项目阶段完成
    @ResponseBody
    @RequestMapping(value = "/project/moment_finish", method = {RequestMethod.GET, RequestMethod.POST})
    public String momentFinish(Integer projectId, Integer momentId) {

        if (momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "momentId Can not be NULL!");
        }

        //因为可以存在多个可以同时进行的阶段，这里不需要判断正在进行的阶段了
//        Moment projectMomentRunning = projectService.getProjectMomentRunning(projectId);
//        if (!projectMomentRunning.getId().equals(momentId)) {
//            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, "Fail");
//        }

        boolean result = projectService.momentFinish(projectId, momentId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    //项目阶段开始
    @ResponseBody
    @RequestMapping(value = "/project/moment_start", method = {RequestMethod.GET, RequestMethod.POST})
    public String momentStart(Integer projectId, Integer momentId) {

        if (momentId == null || projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "momentId Or projectId Can not be NULL!");
        }

        Project project = projectService.getProject(projectId);

        //如果项目不是处在正在进行的状态，不可以开始项目阶段
        if (project.getProjectState().intValue() != 1) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_NOT_STARTED, project);
        }

        //因为可以存在多个可以同时进行的阶段，这里不需要判断正在进行的阶段了
//        Moment projectMomentRunning = projectService.getProjectMomentRunning(projectId);
//        if (projectMomentRunning != null) {
//            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "Another Moment Is Running!", projectMomentRunning);
//        }

        boolean result = projectService.momentStart(projectId, momentId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    //项目完成
    @RequiresPermissions(value = {PermissionStrings.PROJECT_FINISH})
    @ResponseBody
    @RequestMapping(value = "/project/final", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectFinish(Integer projectId) {

        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        //完成项目
        boolean result = projectService.setProjectFinalCheck(projectId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
    }

    //项目关闭
    @RequiresPermissions(value = {PermissionStrings.PROJECT_CLOSE})
    @ResponseBody
    @RequestMapping(value = "/project/close", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectClose(Integer projectId) {

        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        //关闭项目
        boolean result = projectService.setProjectFinished(projectId);

        if (result) {
            // 这里应该在service中使用事务来控制，懒得改，这里不使用临时角色了，因此注释掉
//            Project project = projectService.getProject(projectId);
//            userService.setUserNormal(project.getProjectDirector());
//            userService.setUserNormal(project.getDelegate());
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.OPERATION_FAIL, "Close Fail!");
    }

    //查看项目进度
    @ResponseBody
    @RequestMapping(value = "/project/process", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectProcess(Integer projectId) {

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

        List<Moment> allMoments = projectService.getAllMoments(projectId);

        List<ViewMoment> allViewMoments = new LinkedList<>();

        for (int i = 0; i < allMoments.size(); i++) {
            Moment tempMoment = allMoments.get(i);
            List<Document> allDocsByMoment = documentService.getAllDocsByMoment(tempMoment.getId());
            ViewMoment viewMoment = viewMomentBuild(tempMoment);
            viewMoment.setDocumentList(this.viewDocumentByTypesBuild(allDocsByMoment, tempMoment.getId()));
            allViewMoments.add(viewMoment);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("momentsSize", allMoments.size());
        resultMap.put("moments", allViewMoments);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //查看项目进度的当前信息，比如当前阶段，进度
    @ResponseBody
    @RequestMapping(value = "/project/process_current_detail", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectProcessCurrentDetail(Integer projectId) {

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

        Project project = projectService.getProject(projectId);
        if (project == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_NOT_EXISTS, "Project Is Not Exists!");
        }

        List<Moment> allMoments = projectService.getAllMoments(projectId);

        List<Moment> projectMomentsRunning = projectService.getProjectMomentRunning(projectId);

//        if (projectMomentsRunning.size() == 0) {
//            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.NO_MOMENTS_RUNNING, "没有正在进行的项目阶段！");
//        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("momentsSize", allMoments.size());
        resultMap.put("currentMoment", projectMomentsRunning);
        resultMap.put("project", viewProjectBuild(project));
        resultMap.put("allMoments", allMoments);

        // 由于采用新的进度统计方式，该段代码弃用
//        int uploaded = 0;
//        int total = 0;
//        for (int i = 0; i < allMoments.size(); i++) {
//            uploaded += allMoments.get(i).getUploadedFileNumber();
//            total += allMoments.get(i).getFileNumber();
//        }
//        Double process = new BigDecimal((float)uploaded / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        resultMap.put("process", process.intValue());

        ProjectProcess projectProcessByProject = projectService.getProjectProcessByProject(project.getId());
        if (projectProcessByProject != null) {
            resultMap.put("process", projectProcessByProject.getProjectProcess());
        }else {
            resultMap.put("process", 0);
        }


        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    //获取所有的项目总监，用于下拉列表
    @ResponseBody
    @RequestMapping(value = "/project/directors", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDirectorsForSelect() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewUsersBuild(userService.getAllDirector()));
    }

    //获取所有的总监代表，用于下拉列表
    @ResponseBody
    @RequestMapping(value = "/project/delegates", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDelegatesForSelect() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewUsersBuild(userService.getAllDelegates()));
    }

    //获取所有的监理工程师，用于下拉列表
    @ResponseBody
    @RequestMapping(value = "/project/engineers", method = {RequestMethod.GET, RequestMethod.POST})
    public String allEngineers() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewUsersBuild(userService.getAllEngineers()));
    }

    //获取所有的部门，用于下拉列表
    @ResponseBody
    @RequestMapping(value = "/project/all_departments", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDepartmentForSelect() {
        return departmentService.getAllDepartment();
    }

    //根据项目获取所有的项目阶段，用于下拉列表
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/project/moments", method = {RequestMethod.GET, RequestMethod.POST})
    public String allMoments(Integer projectId) {

        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId cannot be NULL!");
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

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, projectService.getAllMoments(projectId));
    }

    //组装项目视图model
    private ViewProject viewProjectBuild(Project project) {
        ViewProject viewProject = new ViewProject();
        viewProject.setId(project.getId());
        viewProject.setProjectName(project.getProjectName());
        viewProject.setProjectCode(project.getProjectCode());
        viewProject.setClient(project.getClient());
        viewProject.setContractor(project.getContractor());
        viewProject.setDepartment(departmentService.getDepartment(project.getDepartment()));
        viewProject.setPartner(project.getPartner());
        viewProject.setDelegate(userService.getUser(project.getDelegate()));
        viewProject.setProjectDirector(userService.getUser(project.getProjectDirector()));
        viewProject.setCreateFrom(userService.getUser(project.getCreateFrom()));
        viewProject.setProjectDesc(project.getProjectDesc());
        viewProject.setProjectType(project.getProjectType());
        viewProject.setProjectState(project.getProjectState());
        viewProject.setCreateTime(project.getCreateTime());
        viewProject.setFinishTime(project.getFinishTime());
        viewProject.setExpectFinishTime(project.getExpectFinishTime());
        viewProject.setStartTime(project.getStartTime());

//        List<Moment> allMoments = projectService.getAllMoments(project.getId());
//        int uploaded = 0;
//        int total = 0;
//        for (int i = 0; i < allMoments.size(); i++) {
//            uploaded += allMoments.get(i).getUploadedFileNumber();
//            total += allMoments.get(i).getFileNumber();
//        }
//        Double process = new BigDecimal((float)uploaded / total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
//        viewProject.setProcess(process.intValue());

        ProjectProcess projectProcessByProject = projectService.getProjectProcessByProject(project.getId());
        if (projectProcessByProject != null) {
            viewProject.setProcess(projectProcessByProject.getProjectProcess());
        }else {
            viewProject.setProcess(0);
        }

        viewProject.setProjectCost(projectCostService.getProjectCost(project.getId()));

        viewProject.setClientContact(project.getClientContact());
        viewProject.setPersonCharge(project.getPersonCharge());
        viewProject.setAgreementTime(project.getAgreementTime());
        viewProject.setBuildDuration(project.getBuildDuration());
        viewProject.setServiceDuration(project.getServiceDuration());
        viewProject.setIsResident(project.getIsResident());
        viewProject.setProjectBudget(project.getProjectBudget());
        viewProject.setSupervisionAmount(project.getSupervisionAmount());
        viewProject.setSupervisionPaid(project.getSupervisionPaid());
        viewProject.setNotResidentWeekDays(project.getNotResidentWeekDays());

        return viewProject;
    }

    //组装项目视图model
    private List<ViewProject> viewProjectsBuild(List<Project> list) {
        List<ViewProject> viewProjects = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            ViewProject viewProject = viewProjectBuild(list.get(i));
            viewProjects.add(viewProject);
        }
        return viewProjects;
    }

    //组装项目阶段视图model
    private ViewMoment viewMomentBuild(Moment moment) {
        ViewMoment viewMoment = new ViewMoment();
        viewMoment.setId(moment.getId());
        viewMoment.setProjectId(moment.getProjectId());
        viewMoment.setMomentOrder(moment.getMomentOrder());
        viewMoment.setMomentDesc(moment.getMomentDesc());
        viewMoment.setState(moment.getState());
        viewMoment.setFileNumber(moment.getFileNumber());
        viewMoment.setUploadedFileNumber(moment.getUploadedFileNumber());
        viewMoment.setExpectFinishTime(moment.getExpectFinishTime());
        viewMoment.setFinishTime(moment.getFinishTime());
        viewMoment.setStartTime(moment.getStartTime());
        viewMoment.setMomentName(moment.getMomentName());
        return viewMoment;
    }

    //组装用户视图model
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

    //组装用户视图model
    private List<ViewUser> viewUsersBuild(List<User> userList) {
        List<ViewUser> list = new LinkedList<>();
        for (int i = 0; i < userList.size(); i++) {
            ViewUser viewUser = viewUserBuild(userList.get(i));
            list.add(viewUser);
        }
        return list;
    }

    private List<ViewDocumentByType> viewDocumentByTypesBuild(List<Document> allDocs, Integer momentId) {
        List<ViewDocumentByType> viewDocumentByTypes = new LinkedList<>();

        // 获取所有的文档模板
        List<Document> docsByMomentIsTemplate = documentService.listDocsByMomentIsTemplate(momentId);

        // 遍历这个阶段所有
        for (Document docIsTemplate : docsByMomentIsTemplate) {
            ViewDocumentByType viewDocumentByType = new ViewDocumentByType();
            viewDocumentByType.setId(docIsTemplate.getId());  // 设置Id，前端需要根据Id来进行上传
            List<Document> tempList = new LinkedList<>();
            for (int i = 0; i < allDocs.size(); i++) {
                // 遍历该阶段所有文档
                if (allDocs.get(i).getDocType().equals(docIsTemplate.getDocType())) {
                    // 如果docType相等
                    tempList.add(allDocs.get(i));
                }
            }

            viewDocumentByType.setProjectId(docIsTemplate.getProjectId());
            viewDocumentByType.setMomentId(docIsTemplate.getMomentId());
            viewDocumentByType.setDocName(docIsTemplate.getDocName());
            viewDocumentByType.setFileCode(docIsTemplate.getFileCode());
            viewDocumentByType.setDocType(docIsTemplate.getDocType());
            viewDocumentByType.setDocuments(tempList);
            viewDocumentByTypes.add(viewDocumentByType);
        }

        return viewDocumentByTypes;
    }
}
