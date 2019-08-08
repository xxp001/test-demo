package com.supervision.project.service;

import com.supervision.project.model.ProjectTeam;

import java.util.Map;

public interface ProjectTeamService {

    Map<String, Object> getAllTeamMembersInProject(Integer projectId);

    boolean addMemberToProject(ProjectTeam projectTeam);

    boolean removeMemberFromProject(Integer projectId, Integer userId);

    boolean changeDirectorOrDelegate(Integer projectId, Integer userId, Short role);

    boolean isInProject(Integer projectId, Integer userId);

    ProjectTeam getByProjectAndUser(Integer projectId, Integer userId);

}
