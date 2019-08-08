package com.supervision.project.mapper;

import com.supervision.project.model.Project;

import java.util.Date;
import java.util.List;

public interface ProjectMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Project record);

    int insertSelective(Project record);

    Project selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Project record);

    int updateByPrimaryKey(Project record);

    List<Project> selectAll();

    List<Project> selectAllByStateAfter(Short state);

    List<Project> selectAllByQuery(String query);

    List<Project> selectAllByDate(Project record);

    List<Project> selectAllByDateAndStateAfter(Project record);

    List<Project> selectAllByUserByDate(Date start, Date end, Integer userId);

    List<Project> selectAllByUser(Integer userId);
}