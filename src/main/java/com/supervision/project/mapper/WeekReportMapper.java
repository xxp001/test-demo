package com.supervision.project.mapper;

import com.supervision.project.model.WeekReport;

import java.util.Date;
import java.util.List;

public interface WeekReportMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(WeekReport record);

    int insertSelective(WeekReport record);

    WeekReport selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(WeekReport record);

    int updateByPrimaryKey(WeekReport record);

    List<WeekReport> selectAll();

    List<WeekReport> selectAllByProjectId(Integer projectId);

    List<WeekReport> selectAllByUser(Integer userId);

    List<WeekReport> selectAllByDate(Date start, Date end);

    List<WeekReport> selectByDateByProject(Date start, Date end, Integer projectId);

    List<WeekReport> selectAllByUserByDate(Integer userId, Date start, Date end);
}