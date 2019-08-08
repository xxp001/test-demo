package com.supervision.census.controller;

import com.github.pagehelper.PageInfo;
import com.supervision.census.service.CensusService;
import com.supervision.census.utils.ExcelTools;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.StaticProperties;
import com.supervision.project.ViewModel.ViewProject;
import com.supervision.project.model.Moment;
import com.supervision.project.model.Project;
import com.supervision.project.model.ProjectProcess;
import com.supervision.project.service.ProjectCostService;
import com.supervision.project.service.ProjectService;
import com.supervision.user.service.DepartmentService;
import com.supervision.user.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:census controller
 * @Author:TjSanshao
 * @Create:2019-04-13 19:56
 *
 **/
@Controller
public class CensusController {

    @Autowired
    private CensusService censusService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ProjectCostService projectCostService;

    // 返回项目列表，包括进度，用于统计页面的项目统计，进度统计
    @ResponseBody
    @RequestMapping(value = "/census/projects", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.CENSUS_PROJECTS})
    public String projectCensus(String year, Integer page, Integer pageSize) throws ParseException {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (year == null) {
            year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        }

        String startString = year + "-01-01 00:00:00";
        String endString = year + "-12-31 23:59:59";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = simpleDateFormat.parse(startString);
        Date end = simpleDateFormat.parse(endString);

        PageInfo<Project> result = (PageInfo<Project>) projectService.getAllProjectsByDate(start, end, page, pageSize);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", viewProjectsBuild(result.getList()));

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, resultMap);
    }

    @ResponseBody
    @RequestMapping(value = "/census/cost", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.CENSUS_COST})
    public String projectCostCensus(String year, Integer page, Integer pageSize) throws ParseException {
        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        if (year == null) {
            year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        }

        String startString = year + "-01-01 00:00:00";
        String endString = year + "-12-31 23:59:59";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date start = simpleDateFormat.parse(startString);
        Date end = simpleDateFormat.parse(endString);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, censusService.listCensusProjectCostsByDate(start, end, page, pageSize));
    }

    @RequestMapping(value = "/census/export", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.CENSUS_EXPORT})
    public void exportProjects(HttpServletResponse response) throws IOException {
        //开始设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=projects.xls");
        ServletOutputStream outputStream = response.getOutputStream();
        ExcelTools.exportProjectsExcel(ExcelTools.PROJECT_HEADERS, viewProjectsBuild(((PageInfo<Project>)projectService.getAllProjects(1, 1000000)).getList())).write(outputStream);
        outputStream.flush();
        outputStream.close();
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
}
