package com.supervision.project.service.serviceImpl;

import com.supervision.project.mapper.ProjectMapper;
import com.supervision.project.mapper.ProjectTeamMapper;
import com.supervision.project.model.Project;
import com.supervision.project.model.ProjectTeam;
import com.supervision.project.service.ProjectTeamService;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @Project:SupervisionSystem
 * @Description:project team serviceImpl
 * @Author:TjSanshao
 * @Create:2019-02-10 18:19
 *
 **/
@Service
public class ProjectTeamServiceImpl implements ProjectTeamService {

    @Autowired
    private ProjectTeamMapper projectTeamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public Map<String, Object> getAllTeamMembersInProject(Integer projectId) {
        List<ProjectTeam> projectTeams = projectTeamMapper.selectAllByProject(projectId);

        List<User> members = new ArrayList<>();

        User projectManager = new User();
        User delegate = new User();

        for (int i = 0; i < projectTeams.size(); i++) {
            ProjectTeam team = projectTeams.get(i);

            Short role = team.getRoleInProject();

            if (role == 1) {
                delegate = userMapper.selectByPrimaryKey(team.getUserId());
                continue;
            }

            if (role == 2) {
                projectManager = userMapper.selectByPrimaryKey(team.getUserId());
                continue;
            }

            members.add(userMapper.selectByPrimaryKey(team.getUserId()));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("ProjectManager", projectManager);
        result.put("Delegate", delegate);
        result.put("Members", members);

        return result;
    }

    @Override
    public boolean addMemberToProject(ProjectTeam projectTeam) {

        //设置角色
        short role = 0;
        projectTeam.setRoleInProject(role);

        int row = projectTeamMapper.insertSelective(projectTeam);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean removeMemberFromProject(Integer projectId, Integer userId) {

        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setUserId(userId);
        projectTeam.setProjectId(projectId);

        ProjectTeam team = projectTeamMapper.selectByProjectIdAndUserId(projectTeam);

        if (team == null) {
            return false;
        }

        int row = projectTeamMapper.deleteByPrimaryKey(team.getId());

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Transactional
    @Override
    public boolean changeDirectorOrDelegate(Integer projectId, Integer userId, Short role) {

        //查找原来的项目总监或总监代表
        ProjectTeam team = new ProjectTeam();
        team.setProjectId(projectId);
        team.setRoleInProject(role);

        //这里查找到的是根据项目id以及角色查找到的信息
        ProjectTeam old = projectTeamMapper.selectUserByProjectIdAndRole(team);

        //更新项目表中的信息
        Project project = new Project();
        project.setId(projectId);
        if (role.intValue() == 1) {
            project.setDelegate(userId);
        }else if (role.intValue() == 2) {
            project.setProjectDirector(userId);
        }

        int rowInProject = projectMapper.updateByPrimaryKeySelective(project);

        if (rowInProject < 1) {
            return false;
        }

        //更新团队信息
        old.setUserId(userId);
        old.setRoleInProject(role);
        int rowInTeam = projectTeamMapper.updateByPrimaryKeySelective(old);

        if (rowInTeam < 1) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isInProject(Integer projectId, Integer userId) {

        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setUserId(userId);
        projectTeam.setProjectId(projectId);

        ProjectTeam team = projectTeamMapper.selectByProjectIdAndUserId(projectTeam);

        if (team == null) {
            return false;
        }

        return true;
    }

    @Override
    public ProjectTeam getByProjectAndUser(Integer projectId, Integer userId) {
        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setUserId(userId);
        projectTeam.setProjectId(projectId);

        ProjectTeam team = projectTeamMapper.selectByProjectIdAndUserId(projectTeam);

        return team;
    }
}
