package com.supervision.project.service;

import com.github.pagehelper.PageInfo;
import com.supervision.project.model.Daily;
import com.supervision.project.model.WeekReport;

import java.util.Date;
import java.util.List;

public interface ProjectDailyService {

    Object getAllDaily(int page, int pageSize);

    PageInfo<Daily> listAllDailyByDateByPage(int page, int pageSize, Date start, Date end);

    PageInfo<Daily> listAllDailyByDateByPageInProject(int page, int pageSize, Date start, Date end, int project);

    Object getAllDailyByProject(Integer projectId, int page, int pageSize);

    Object getAllWeekReport(int page, int pageSize);

    PageInfo<WeekReport> listAllWeekReportByDate(int page, int pageSize, Date start, Date end);

    PageInfo<WeekReport> listAllWeekReportByDateByProject(int page, int pageSize, Date start, Date end, int project);

    PageInfo<WeekReport> listAllWeekReportByUserByDate(int page, int pageSize, int userId, Date start, Date end);

    PageInfo<WeekReport> getAllWeekReportByUser(int userId, int page, int pageSize);

    Object getAllWeekReportByProject(Integer projectId, int page, int pageSize);

    Daily getDaily(Integer id);

    PageInfo<Daily> getAllDailyByUser(Integer userId, Integer page, Integer pageSize);

    PageInfo<Daily> getAllDailyByUserByPage(Integer page, Integer pageSize, Integer userId);

    PageInfo<Daily> getAllDailyByUserByPageByDate(Integer page, Integer pageSize, Integer userId, Date start, Date end);

    boolean addProjectDaily(Daily daily);

    boolean addProjectWeekReport(WeekReport report);

}
