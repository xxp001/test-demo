package com.supervision.project.mapper;

import com.supervision.project.model.ProjectTeam;
import com.supervision.user.model.User;

import java.util.List;

public interface ProjectTeamMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProjectTeam record);

    int insertSelective(ProjectTeam record);

    ProjectTeam selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProjectTeam record);

    int updateByPrimaryKey(ProjectTeam record);

    List<ProjectTeam> selectAllByProject(Integer projectId);

    ProjectTeam selectByProjectIdAndUserId(ProjectTeam record);

    ProjectTeam selectUserByProjectIdAndRole(ProjectTeam record);

    List<ProjectTeam> selectAllByUser(Integer userId);
}