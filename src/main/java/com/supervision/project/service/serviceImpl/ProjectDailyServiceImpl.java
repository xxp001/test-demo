package com.supervision.project.service.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.project.mapper.DailyMapper;
import com.supervision.project.mapper.WeekReportMapper;
import com.supervision.project.model.Daily;
import com.supervision.project.model.WeekReport;
import com.supervision.project.service.ProjectDailyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:project daily serviceImpl
 * @Author:TjSanshao
 * @Create:2019-02-08 13:45
 *
 **/
@Service
public class ProjectDailyServiceImpl implements ProjectDailyService {

    @Autowired
    private DailyMapper dailyMapper;

    @Autowired
    private WeekReportMapper weekReportMapper;

    @Override
    public Object getAllDaily(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectAll();
        return new PageInfo<>(dailies);
    }

    @Override
    public PageInfo<Daily> listAllDailyByDateByPage(int page, int pageSize, Date start, Date end) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectAllByDate(start, end);
        return new PageInfo<>(dailies);
    }

    @Override
    public PageInfo<Daily> listAllDailyByDateByPageInProject(int page, int pageSize, Date start, Date end, int project) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectByDateByProject(start, end, project);
        return new PageInfo<>(dailies);
    }

    @Override
    public Object getAllDailyByProject(Integer projectId, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectAllByProjectId(projectId);
        return new PageInfo<>(dailies);
    }

    @Override
    public Object getAllWeekReport(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectAll();
        return new PageInfo<>(weekReports);
    }

    @Override
    public PageInfo<WeekReport> listAllWeekReportByDate(int page, int pageSize, Date start, Date end) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectAllByDate(start, end);
        return new PageInfo<>(weekReports);
    }

    @Override
    public PageInfo<WeekReport> listAllWeekReportByDateByProject(int page, int pageSize, Date start, Date end, int project) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectByDateByProject(start, end, project);
        return new PageInfo<>(weekReports);
    }

    @Override
    public PageInfo<WeekReport> listAllWeekReportByUserByDate(int page, int pageSize, int userId, Date start, Date end) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectAllByUserByDate(userId, start, end);
        return new PageInfo<>(weekReports);
    }

    @Override
    public PageInfo<WeekReport> getAllWeekReportByUser(int userId, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectAllByUser(userId);
        return new PageInfo<>(weekReports);
    }

    @Override
    public Object getAllWeekReportByProject(Integer projectId, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<WeekReport> weekReports = weekReportMapper.selectAllByProjectId(projectId);
        return new PageInfo<>(weekReports);
    }

    @Override
    public Daily getDaily(Integer id) {
        return dailyMapper.selectByPrimaryKey(id);
    }

    @Override
    public PageInfo<Daily> getAllDailyByUser(Integer userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectByUploader(userId);
        return new PageInfo<>(dailies);
    }

    @Override
    public PageInfo<Daily> getAllDailyByUserByPage(Integer page, Integer pageSize, Integer userId) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectByUploader(userId);
        return new PageInfo<>(dailies);
    }

    @Override
    public PageInfo<Daily> getAllDailyByUserByPageByDate(Integer page, Integer pageSize, Integer userId, Date start, Date end) {
        PageHelper.startPage(page, pageSize);
        List<Daily> dailies = dailyMapper.selectByUploaderByDate(userId, start, end);
        return new PageInfo<>(dailies);
    }

    @Override
    public boolean addProjectDaily(Daily daily) {
        int row = dailyMapper.insertSelective(daily);
        if (row > 0) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean addProjectWeekReport(WeekReport report) {
        int row = weekReportMapper.insertSelective(report);
        if (row > 0) {
            return true;
        }else {
            return false;
        }
    }
}
