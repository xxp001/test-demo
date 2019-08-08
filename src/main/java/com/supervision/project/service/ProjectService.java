package com.supervision.project.service;

import com.github.pagehelper.PageInfo;
import com.supervision.project.ViewModel.ViewDailyAndHumanCost;
import com.supervision.project.model.*;

import java.util.Date;
import java.util.List;

public interface ProjectService {

    int addProject(Project project);

    boolean addCustomProject(CustomProject customProject);

    boolean addCustomMoment(CustomMoment customMoment);

    boolean addDailyAndHumanCost(ViewDailyAndHumanCost viewDailyAndHumanCost, Integer uploadFrom);

    Project getProject(int projectId);

    Object getAllProjects(int page, int pageSize);

    PageInfo<Project> getAllProjectsByUserWithPageInfo(int page, int pageSize, int userId);

    Object getAllProjectsByUser(int page, int pageSize, Integer userId);

    Object getAllProjectsByState(int page, int pageSize, Short state);

    Object getAllProjectsByQuery(String query, Integer page, Integer pageSize);

    Object getAllProjectsByQueryByUser(String query, Integer userId, Integer page, Integer pageSize);

    Object getAllProjectsByStateByUser(int page, int pageSize, Short state, Integer userId);

    Object getAllProjectsByDate(Date start, Date end, Integer page, Integer pageSize);

    Object getAllProjectsByDate(Date start, Date end, Integer page, Integer pageSize, Integer userId);

    boolean setProjectStart(int projectId);

    boolean setProjectFinalCheck(int projectId);

    boolean setProjectFinished(int projectId);

    List<Moment> getAllMoments(int projectId);

    boolean momentStart(int projectId, int momentId);

    boolean momentFinish(int projectId, int momentId);

    List<Moment> getProjectMomentRunning(int projectId);  //每个项目可以有多个项目阶段处于正在进行阶段，即state=1

    Moment getMoment(int momentId);

    List<ProjectType> getAllProjectTypes();

    ProjectProcess getProjectProcessByProject(int projectId);
    
    //ProjectProcess getProjectProcessByProjectState(int projectId,int projectState);
}
