package com.supervision.project.controller;

import com.github.pagehelper.PageInfo;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import com.supervision.project.ViewModel.ViewDaily;
import com.supervision.project.ViewModel.ViewDailyAndHumanCost;
import com.supervision.project.ViewModel.ViewWeekReport;
import com.supervision.project.model.Daily;
import com.supervision.project.model.HumanCostRecord;
import com.supervision.project.model.WeekReport;
import com.supervision.project.service.ProjectCostService;
import com.supervision.project.service.ProjectDailyService;
import com.supervision.project.service.ProjectService;
import com.supervision.project.service.ProjectTeamService;
import com.supervision.user.model.User;
import com.supervision.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:project daily controller
 * @Author:TjSanshao
 * @Create:2019-02-08 12:28
 *
 **/
@Controller
@CrossOrigin
@RequiresAuthentication
public class ProjectDailyController {

    @Autowired
    private ProjectDailyService projectDailyService;

    @Autowired
    private ProjectTeamService projectTeamService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectCostService projectCostService;

    // 查看提交的工作日志
    @ResponseBody
    @RequestMapping(value = "/project/my_work_daily", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectPersonDaily(Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        // 获取登录用户信息
        Session session = SecurityUtils.getSubject().getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Daily> allDailyByUserByPage = projectDailyService.getAllDailyByUserByPage(page, pageSize, userInLogin.getId());

        List<Daily> allDailyByUser = allDailyByUserByPage.getList();

        List<HumanCostRecord> allHumanCostRecord = new LinkedList<>();

        List<ViewDaily> allViewDaily = new LinkedList<>();

        //在查询到daily之后，根据daily的属性，查找daily对应的工作记录（这里的日志和工作记录是唯一的对应关系）
        //这个关系需要只允许通过首页的工作日志的填写来保持唯一
        for (int i = 0; i < allDailyByUser.size(); i++) {
            Daily daily = allDailyByUser.get(i);
            HumanCostRecord humanCostRecordByUploaderAndProjectAndUploadTime = projectCostService.getHumanCostRecordByUploaderAndProjectAndUploadTime(daily.getUploadFrom(), daily.getProjectId(), daily.getUploadTime());
            allHumanCostRecord.add(humanCostRecordByUploaderAndProjectAndUploadTime);

            //创建ViewDaily
            ViewDaily viewDaily = viewDailyBuild(daily);
            allViewDaily.add(viewDaily);
        }

        //这里使用视图对象返回数据，这里的两个list，将会是下标对应关系
        ViewDailyAndHumanCost viewDailyAndHumanCost = new ViewDailyAndHumanCost();
        viewDailyAndHumanCost.setDailyListObject(allDailyByUser);
        viewDailyAndHumanCost.setCostRecordListObject(allHumanCostRecord);
        //这里为了方便查询project信息，封装ViewDaily，这个list也会死下标对应关系
        viewDailyAndHumanCost.setViewDailyList(allViewDaily);

        //把分页信息封装
        viewDailyAndHumanCost.setTotal(allDailyByUserByPage.getTotal());
        viewDailyAndHumanCost.setPageNum(allDailyByUserByPage.getPageNum());
        viewDailyAndHumanCost.setPageSize(allDailyByUserByPage.getPageSize());
        viewDailyAndHumanCost.setPages(allDailyByUserByPage.getPages());

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewDailyAndHumanCost);
    }

    // 查看提交的工作日志（按照时间段）
    @ResponseBody
    @RequestMapping(value = "/project/my_work_daily_date", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectPersonDailyByDate(Integer page, Integer pageSize, Date start, Date end) throws ParseException {
        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }
        if (start == null) {
            start = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("1996-06-14 00:00:00");
        }
        if (end == null) {
            end = new Date();
        }

        // 获取登录用户信息
        Session session = SecurityUtils.getSubject().getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Daily> allDailyByUserByPage = projectDailyService.getAllDailyByUserByPageByDate(page, pageSize, userInLogin.getId(), start, end);

        List<Daily> allDailyByUser = allDailyByUserByPage.getList();

        List<HumanCostRecord> allHumanCostRecord = new LinkedList<>();

        List<ViewDaily> allViewDaily = new LinkedList<>();

        //在查询到daily之后，根据daily的属性，查找daily对应的工作记录（这里的日志和工作记录是唯一的对应关系）
        //这个关系需要只允许通过首页的工作日志的填写来保持唯一
        for (int i = 0; i < allDailyByUser.size(); i++) {
            Daily daily = allDailyByUser.get(i);
            HumanCostRecord humanCostRecordByUploaderAndProjectAndUploadTime = projectCostService.getHumanCostRecordByUploaderAndProjectAndUploadTime(daily.getUploadFrom(), daily.getProjectId(), daily.getUploadTime());
            allHumanCostRecord.add(humanCostRecordByUploaderAndProjectAndUploadTime);

            //创建ViewDaily
            ViewDaily viewDaily = viewDailyBuild(daily);
            allViewDaily.add(viewDaily);
        }

        //这里使用视图对象返回数据，这里的两个list，将会是下标对应关系
        ViewDailyAndHumanCost viewDailyAndHumanCost = new ViewDailyAndHumanCost();
        viewDailyAndHumanCost.setDailyListObject(allDailyByUser);
        viewDailyAndHumanCost.setCostRecordListObject(allHumanCostRecord);
        //这里为了方便查询project信息，封装ViewDaily，这个list也会死下标对应关系
        viewDailyAndHumanCost.setViewDailyList(allViewDaily);

        //把分页信息封装
        viewDailyAndHumanCost.setTotal(allDailyByUserByPage.getTotal());
        viewDailyAndHumanCost.setPageNum(allDailyByUserByPage.getPageNum());
        viewDailyAndHumanCost.setPageSize(allDailyByUserByPage.getPageSize());
        viewDailyAndHumanCost.setPages(allDailyByUserByPage.getPages());

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewDailyAndHumanCost);
    }

    // 查看工作日志
    @ResponseBody
    @RequestMapping(value = "/project/work_daily", method = {RequestMethod.GET, RequestMethod.POST})
    public String personDaily(Integer dailyId) {

        if (dailyId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DAILY_ID_CANNOT_BE_NULL, null);
        }

        Daily daily = projectDailyService.getDaily(dailyId);
        HumanCostRecord humanCostRecord = projectCostService.getHumanCostRecordByUploaderAndProjectAndUploadTime(daily.getUploadFrom(), daily.getProjectId(), daily.getUploadTime());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("daily", viewDailyBuild(daily));
        resultMap.put("humanRecord", humanCostRecord);
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看一个项目的所有日志
    @ResponseBody
    @RequestMapping(value = "/project/daily", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectDaily(Integer projectId, Integer page, Integer pageSize) {

        //必须传入projectId
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
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

        PageInfo<Daily> allDailyByProjectPageInfo = (PageInfo<Daily>) projectDailyService.getAllDailyByProject(projectId, page, pageSize);

        List<Daily> allDaily = (allDailyByProjectPageInfo).getList();

        List<ViewDaily> viewDailies = new ArrayList<>();

        for (int i = 0; i < allDaily.size(); i++) {
            viewDailies.add(viewDailyBuild(allDaily.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allDailyByProjectPageInfo.getTotal());
        resultMap.put("pages", allDailyByProjectPageInfo.getPages());
        resultMap.put("page", allDailyByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allDailyByProjectPageInfo.getPageSize());
        resultMap.put("list", viewDailies);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 获取projectId指定的项目所有日志（根据日期范围）
    @RequiresPermissions(value = {PermissionStrings.PROJECT_READ_ALL})
    @ResponseBody
    @RequestMapping(value = "/project/dailies", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDailies(Integer page, Integer pageSize, Date start, Date end, Integer projectId) throws ParseException {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (start == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            start = simpleDateFormat.parse("1996-06-14 00:00:00");
            if (end == null) {
                end = new Date();
            }
        }
        if (end == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.DATE, 1);
            end = calendar.getTime();
        }

        PageInfo<Daily> allDailyByProjectPageInfo = projectDailyService.listAllDailyByDateByPageInProject(page, pageSize, start, end, projectId);

        List<Daily> allDaily = (allDailyByProjectPageInfo).getList();

        List<ViewDaily> viewDailies = new ArrayList<>();

        for (int i = 0; i < allDaily.size(); i++) {
            viewDailies.add(viewDailyBuild(allDaily.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allDailyByProjectPageInfo.getTotal());
        resultMap.put("pages", allDailyByProjectPageInfo.getPages());
        resultMap.put("page", allDailyByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allDailyByProjectPageInfo.getPageSize());
        resultMap.put("list", viewDailies);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }


    // 获取自己所有日志
    @ResponseBody
    @RequestMapping(value = "/project/my_dailies", method = {RequestMethod.GET, RequestMethod.POST})
    public String myAllDailies(Integer page, Integer pageSize, Date start, Date end) throws ParseException {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (start == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            start = simpleDateFormat.parse("1996-06-14 00:00:00");
        }
        if (end == null) {
            end = new Date();
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<Daily> allDailyByProjectPageInfo = projectDailyService.getAllDailyByUserByPageByDate(page, pageSize, userInLogin.getId(), start, end);

        List<Daily> allDaily = (allDailyByProjectPageInfo).getList();

        List<ViewDaily> viewDailies = new ArrayList<>();

        for (int i = 0; i < allDaily.size(); i++) {
            viewDailies.add(viewDailyBuild(allDaily.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allDailyByProjectPageInfo.getTotal());
        resultMap.put("pages", allDailyByProjectPageInfo.getPages());
        resultMap.put("page", allDailyByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allDailyByProjectPageInfo.getPageSize());
        resultMap.put("list", viewDailies);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看所有周报
    @ResponseBody
    @RequestMapping(value = "/project/reports", method = {RequestMethod.GET, RequestMethod.POST})
    public String reports(Integer page, Integer pageSize, Date start, Date end, Integer projectId) throws ParseException {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (start == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            start = simpleDateFormat.parse("1996-06-14 00:00:00");
            if (end == null) {
                end = new Date();
            }
        }
        if (end == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.DATE, 1);
            end = calendar.getTime();
        }

        PageInfo<WeekReport> allWeekReportByProjectPageInfo = projectDailyService.listAllWeekReportByDateByProject(page, pageSize, start, end, projectId);

        List<WeekReport> allReport = allWeekReportByProjectPageInfo.getList();

        List<ViewWeekReport> viewWeekReports = new ArrayList<>();

        for (int i = 0; i < allReport.size(); i++) {
            viewWeekReports.add(viewWeekReportBuild(allReport.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allWeekReportByProjectPageInfo.getTotal());
        resultMap.put("pages", allWeekReportByProjectPageInfo.getPages());
        resultMap.put("page", allWeekReportByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allWeekReportByProjectPageInfo.getPageSize());
        resultMap.put("list", viewWeekReports);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看自己所有周报
    @ResponseBody
    @RequestMapping(value = "/project/my_reports", method = {RequestMethod.GET, RequestMethod.POST})
    public String myReports(Integer page, Integer pageSize, Date start, Date end) throws ParseException {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (start == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            start = simpleDateFormat.parse("1996-06-14 00:00:00");
        }
        if (end == null) {
            end = new Date();
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        PageInfo<WeekReport> allWeekReportByProjectPageInfo = projectDailyService.listAllWeekReportByUserByDate(page, pageSize, userInLogin.getId(), start, end);

        List<WeekReport> allReport = allWeekReportByProjectPageInfo.getList();

        List<ViewWeekReport> viewWeekReports = new ArrayList<>();

        for (int i = 0; i < allReport.size(); i++) {
            viewWeekReports.add(viewWeekReportBuild(allReport.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allWeekReportByProjectPageInfo.getTotal());
        resultMap.put("pages", allWeekReportByProjectPageInfo.getPages());
        resultMap.put("page", allWeekReportByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allWeekReportByProjectPageInfo.getPageSize());
        resultMap.put("list", viewWeekReports);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 查看一个项目的所有周报
    @ResponseBody
    @RequestMapping(value = "/project/week_report", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectWeekReport(Integer projectId, Integer page, Integer pageSize) {

        //必须传入projectId
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
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

        PageInfo<WeekReport> allWeekReportByProjectPageInfo = (PageInfo<WeekReport>) projectDailyService.getAllWeekReportByProject(projectId, page, pageSize);

        List<WeekReport> allReport = allWeekReportByProjectPageInfo.getList();

        List<ViewWeekReport> viewWeekReports = new ArrayList<>();

        for (int i = 0; i < allReport.size(); i++) {
            viewWeekReports.add(viewWeekReportBuild(allReport.get(i)));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", allWeekReportByProjectPageInfo.getTotal());
        resultMap.put("pages", allWeekReportByProjectPageInfo.getPages());
        resultMap.put("page", allWeekReportByProjectPageInfo.getPageNum());
        resultMap.put("pageSize", allWeekReportByProjectPageInfo.getPageSize());
        resultMap.put("list", viewWeekReports);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    // 添加工作日志（项目为单位，挂在项目上）
    @ResponseBody
    @RequestMapping(value = "/project/daily_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectDailyAdd(Daily daily) {

        if (daily.getProjectId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(daily.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        daily.setUploadFrom(userInLogin.getId());

        //设置上传时间
        if (daily.getUploadTime() == null) {
            daily.setUploadTime(new Date());
        }

        boolean result = projectDailyService.addProjectDaily(daily);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, daily);
    }

    // 添加周报（项目为单位，挂在项目上）
    @ResponseBody
    @RequestMapping(value = "/project/week_report_build", method = {RequestMethod.GET, RequestMethod.POST})
    public String projectWeekReportAdd(WeekReport report) {

        if (report.getProjectId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(report.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        if (report.getUploadTime() == null) {
            report.setUploadTime(new Date());
        }

        report.setUploadFrom(userInLogin.getId());

        boolean result = projectDailyService.addProjectWeekReport(report);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, report);
    }

    // 组装daily视图model
    private ViewDaily viewDailyBuild(Daily daily) {
        ViewDaily viewDaily = new ViewDaily();
        viewDaily.setId(daily.getId());
        viewDaily.setProject(projectService.getProject(daily.getProjectId()));
        viewDaily.setSummary(daily.getSummary());
        viewDaily.setDailyDetail(daily.getDailyDetail());
        viewDaily.setUploadTime(daily.getUploadTime());
        viewDaily.setUploadFrom(userService.getUser(daily.getUploadFrom()));
        viewDaily.setDailyDetailAM(daily.getDailyDetailAM());
        viewDaily.setDailyDetailPM(daily.getDailyDetailPM());
        return viewDaily;
    }

    // 组装weekReport视图model
    private ViewWeekReport viewWeekReportBuild(WeekReport report) {
        ViewWeekReport viewWeekReport = new ViewWeekReport();
        viewWeekReport.setId(report.getId());
        viewWeekReport.setProject(projectService.getProject(report.getProjectId()));
        viewWeekReport.setSummary(report.getSummary());
        viewWeekReport.setWeekDetail(report.getWeekDetail());
        viewWeekReport.setUploadTime(report.getUploadTime());
        viewWeekReport.setUploadFrom(userService.getUser(report.getUploadFrom()));
        viewWeekReport.setAskQuestion(report.getAskQuestion());
        return viewWeekReport;
    }

}
