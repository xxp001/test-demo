package com.supervision.project.service;

import com.github.pagehelper.PageInfo;
import com.supervision.project.ViewModel.ViewCost;
import com.supervision.project.model.*;
import com.supervision.user.model.Role;

import java.util.Date;
import java.util.List;

public interface ProjectCostService {

    List<HumanCostRecord> getAllHumanCostRecord(int projectId);

    List<ExtraCost> getAllExtraCost(int projectId);

    List<ExtraCost> listExtraCostByUser(int projectId, int userId);

    List<ExtraCost> listExtraCostByUserByTime(int projectId, int userId, Date uploadTime);

    List<ExtraCost> listExtraCostByUserByTimeInMoment(int momentId, int userId, Date uploadTime);

    List<HumanCostRecord> listHumanCostRecordByUser(int projectId, int userId);

    PageInfo<HumanCostRecord> listHumanCostRecordByUserByPage(int projectId, int userId, int page, int pageSize);

    List<HumanCostRecord> listHumanCostRecordByUserInMoment(int momentId, int userId);

    PageInfo<HumanCostRecord> listHumanCostRecordByUserInMomentByPage(int momentId, int userId, int page, int pageSize);

    ProjectCost getProjectCost(int projectId);

    List<MomentCost> getMomentCosts(int projectId);

    boolean addHumanCostRecord(HumanCostRecord record);

    boolean addExtraCost(ExtraCost extraCost);

    boolean throughHumanCost(int recordId);

    boolean throughExtraCost(int extraId);

    boolean againstHumanCost(int recordId);

    boolean againstExtraCost(int extraId);

    List<RoleCost> getAllRoleCost();

    RoleCost getRoleCostByUser(Integer userId);

    boolean isExistRoleCost(int roleId);

    boolean addRoleCost(RoleCost roleCost);

    boolean updateRoleCost(RoleCost roleCost);

    List<HumanCostRecord> getAllHumanCostRecordByMoment(int momentId);

    HumanCostRecord getHumanCostRecordByUploaderAndProjectAndUploadTime(Integer uploader, Integer projectId, Date uploadTime);

    List<ExtraCost> getAllExtraCostByMoment(int momentId);

    boolean addCost(ViewCost viewCost);

    Role getRoleById(Integer id);

}
