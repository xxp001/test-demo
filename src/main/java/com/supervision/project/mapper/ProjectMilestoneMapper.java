package com.supervision.project.mapper;

import com.supervision.project.model.ProjectMilestone;

public interface ProjectMilestoneMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProjectMilestone record);

    int insertSelective(ProjectMilestone record);

    ProjectMilestone selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProjectMilestone record);

    int updateByPrimaryKey(ProjectMilestone record);

    ProjectMilestone selectByTemplateName(String template);
}