package com.supervision.project.mapper;

import com.supervision.project.model.Daily;

import java.util.Date;
import java.util.List;

public interface DailyMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Daily record);

    int insertSelective(Daily record);

    Daily selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Daily record);

    int updateByPrimaryKey(Daily record);

    List<Daily> selectAll();

    List<Daily> selectAllByProjectId(Integer projectId);

    List<Daily> selectByUploader(Integer userId);

    List<Daily> selectByUploaderByDate(Integer userId, Date start, Date end);

    List<Daily> selectAllByDate(Date start, Date end);

    List<Daily> selectByDateByProject(Date start, Date end, Integer projectId);
}