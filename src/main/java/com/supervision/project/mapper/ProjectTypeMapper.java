package com.supervision.project.mapper;

import com.supervision.project.model.ProjectType;

import java.util.List;

public interface ProjectTypeMapper {
    int deleteByPrimaryKey(Integer typeId);

    int insert(ProjectType record);

    int insertSelective(ProjectType record);

    ProjectType selectByPrimaryKey(Integer typeId);

    int updateByPrimaryKeySelective(ProjectType record);

    int updateByPrimaryKey(ProjectType record);

    List<ProjectType> selectAll();
}