package com.supervision.project.mapper;

import org.apache.ibatis.annotations.Param;

import com.supervision.project.model.ProjectProcess;

public interface ProjectProcessMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProjectProcess record);

    int insertSelective(ProjectProcess record);

    ProjectProcess selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProjectProcess record);

    int updateByPrimaryKey(ProjectProcess record);

    ProjectProcess selectByProject(Integer projectId);
    
    //ProjectProcess selectByProjectState(@Param("projectId") Integer projectId,@Param("projectState") Integer projectState);
}