package com.supervision.project.mapper;

import com.supervision.project.model.ProjectCost;

public interface ProjectCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProjectCost record);

    int insertSelective(ProjectCost record);

    ProjectCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProjectCost record);

    int updateByPrimaryKey(ProjectCost record);

    ProjectCost selectByProjectId(int projectId);
}