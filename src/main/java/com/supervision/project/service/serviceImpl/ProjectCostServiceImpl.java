package com.supervision.project.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.project.ViewModel.ViewCost;
import com.supervision.project.mapper.*;
import com.supervision.project.model.*;
import com.supervision.project.service.ProjectCostService;
import com.supervision.user.mapper.RoleMapper;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.model.Role;
import com.supervision.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:project cost serviceImpl
 * @Author:TjSanshao
 * @Create:2019-02-10 12:47
 *
 **/
@Service
public class ProjectCostServiceImpl implements ProjectCostService {

    @Autowired
    private HumanCostRecordMapper humanCostRecordMapper;

    @Autowired
    private ExtraCostMapper extraCostMapper;

    @Autowired
    private MomentCostMapper momentCostMapper;

    @Autowired
    private ProjectCostMapper projectCostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleCostMapper roleCostMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<HumanCostRecord> getAllHumanCostRecord(int projectId) {
        return humanCostRecordMapper.selectByProjectId(projectId);
    }

    @Override
    public List<ExtraCost> getAllExtraCost(int projectId) {
        return extraCostMapper.selectByProjectId(projectId);
    }

    @Override
    public List<ExtraCost> listExtraCostByUser(int projectId, int userId) {
        return extraCostMapper.selectByProjectAndUser(projectId, userId);
    }

    @Override
    public List<ExtraCost> listExtraCostByUserByTime(int projectId, int userId, Date uploadTime) {
        return extraCostMapper.selectByProjectAndUserByTime(projectId, userId, uploadTime);
    }

    @Override
    public List<ExtraCost> listExtraCostByUserByTimeInMoment(int momentId, int userId, Date uploadTime) {
        return extraCostMapper.selectByMomentAndUserByTime(momentId, userId, uploadTime);
    }

    @Override
    public List<HumanCostRecord> listHumanCostRecordByUser(int projectId, int userId) {
        return humanCostRecordMapper.selectByProjectAndUser(projectId, userId);
    }

    @Override
    public PageInfo<HumanCostRecord> listHumanCostRecordByUserByPage(int projectId, int userId, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<HumanCostRecord> humanCostRecords = humanCostRecordMapper.selectByProjectAndUser(projectId, userId);
        return new PageInfo<>(humanCostRecords);
    }

    @Override
    public List<HumanCostRecord> listHumanCostRecordByUserInMoment(int momentId, int userId) {
        return humanCostRecordMapper.selectByMomentAndUser(momentId, userId);
    }

    @Override
    public PageInfo<HumanCostRecord> listHumanCostRecordByUserInMomentByPage(int momentId, int userId, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<HumanCostRecord> humanCostRecords = humanCostRecordMapper.selectByMomentAndUser(momentId, userId);
        return new PageInfo<>(humanCostRecords);
    }

    @Override
    public ProjectCost getProjectCost(int projectId) {
        return projectCostMapper.selectByProjectId(projectId);
    }

    @Override
    public List<MomentCost> getMomentCosts(int projectId) {
        return momentCostMapper.selectByProjectId(projectId);
    }

    @Override
    public boolean addHumanCostRecord(HumanCostRecord record) {
        User user = userMapper.selectByPrimaryKey(record.getUploadFrom());
        record.setWorkCode(user.getWorkCode());
        record.setUploaderName(user.getUserRealname());
        int row = humanCostRecordMapper.insertSelective(record);
        if (row > 0) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean addExtraCost(ExtraCost extraCost) {
        User user = userMapper.selectByPrimaryKey(extraCost.getUploadFrom());
        extraCost.setWorkCode(user.getWorkCode());
        extraCost.setUploaderName(user.getUserRealname());
        int row = extraCostMapper.insertSelective(extraCost);
        if (row > 0) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean throughHumanCost(int recordId) {
        HumanCostRecord humanCostRecord = humanCostRecordMapper.selectByPrimaryKey(recordId);
        short state = 1;  //1表示审核通过
        humanCostRecord.setApplyState(state);

        int row = humanCostRecordMapper.updateByPrimaryKey(humanCostRecord);//更新数据

        MomentCost query = new MomentCost();
        query.setProjectId(humanCostRecord.getProjectId());
        query.setMomentId(humanCostRecord.getMomentId());
        MomentCost momentCost = momentCostMapper.selectByProjectIdAndMomentId(query);
        momentCost.setRealCost(momentCost.getRealCost().add(roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay().multiply(new BigDecimal(humanCostRecord.getWorkTime()))));
        momentCostMapper.updateByPrimaryKeySelective(momentCost);

        ProjectCost projectCost = projectCostMapper.selectByProjectId(humanCostRecord.getProjectId());
        projectCost.setRealCost(projectCost.getRealCost().add(roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay().multiply(new BigDecimal(humanCostRecord.getWorkTime()))));
        projectCostMapper.updateByPrimaryKeySelective(projectCost);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean throughExtraCost(int extraId) {
        ExtraCost extraCost = extraCostMapper.selectByPrimaryKey(extraId);
        short state = 1;  //1表示审核通过
        extraCost.setApplyState(state);

        int row = extraCostMapper.updateByPrimaryKey(extraCost);//更新数据

        //更新阶段成本
        MomentCost query = new MomentCost();
        query.setProjectId(extraCost.getProjectId());
        query.setMomentId(extraCost.getMomentId());
        MomentCost momentCost = momentCostMapper.selectByProjectIdAndMomentId(query);
        momentCost.setRealCost(momentCost.getRealCost().add(extraCost.getAmount()));
        int rowInMomentCost = momentCostMapper.updateByPrimaryKey(momentCost);

        //更新项目成本
        ProjectCost projectCost = projectCostMapper.selectByProjectId(extraCost.getProjectId());
        projectCost.setRealCost(projectCost.getRealCost().add(extraCost.getAmount()));
        int rowInProjectCost = projectCostMapper.updateByPrimaryKey(projectCost);

        if (row > 0 && rowInMomentCost > 0 && rowInProjectCost > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean againstHumanCost(int recordId) {
        HumanCostRecord humanCostRecord = humanCostRecordMapper.selectByPrimaryKey(recordId);
        short state = 2;  //2表示拒绝申请
        humanCostRecord.setApplyState(state);
        int row = humanCostRecordMapper.updateByPrimaryKey(humanCostRecord);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean againstExtraCost(int extraId) {
        ExtraCost extraCost = extraCostMapper.selectByPrimaryKey(extraId);
        short state = 2;
        extraCost.setApplyState(state);
        int row = extraCostMapper.updateByPrimaryKey(extraCost);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public List<RoleCost> getAllRoleCost() {
        return roleCostMapper.selectAll();
    }

    @Override
    public RoleCost getRoleCostByUser(Integer userId) {
        Integer roleId = userMapper.selectByPrimaryKey(userId).getRoleId();
        return roleCostMapper.selectByPrimaryKey(roleId);
    }

    @Override
    public boolean isExistRoleCost(int roleId) {
        RoleCost roleCost = roleCostMapper.selectByPrimaryKey(roleId);
        if (roleCost == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean addRoleCost(RoleCost roleCost) {

        int row = roleCostMapper.insertSelective(roleCost);

        if (row > 0) {
            return true;
        }

        return false;

    }

    @Override
    public boolean updateRoleCost(RoleCost roleCost) {

        int row = roleCostMapper.updateByPrimaryKeySelective(roleCost);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public List<HumanCostRecord> getAllHumanCostRecordByMoment(int momentId) {
        return humanCostRecordMapper.selectByMoment(momentId);
    }

    @Override
    public HumanCostRecord getHumanCostRecordByUploaderAndProjectAndUploadTime(Integer uploader, Integer projectId, Date uploadTime) {

        HumanCostRecord humanCostRecord = new HumanCostRecord();
        humanCostRecord.setUploadFrom(uploader);
        humanCostRecord.setUploadTime(uploadTime);
        humanCostRecord.setProjectId(projectId);

        //假设同一时间用户上传的工作记录中，在项目id的约束下，唯一

        return humanCostRecordMapper.selectHumanCostRecordByUploaderAndProjectAndUploadTime(humanCostRecord);

    }

    @Override
    public List<ExtraCost> getAllExtraCostByMoment(int momentId) {
        return extraCostMapper.selectByMoment(momentId);
    }

    @Transactional
    @Override
    public boolean addCost(ViewCost viewCost) {

        HumanCostRecord lastOne = humanCostRecordMapper.selectTheLastRecordByUpload(viewCost.getUploadFrom(), viewCost.getProjectId());

        String extraCostListStrings = viewCost.getExtraCostList();
        HumanCostRecord humanCostRecord = viewCost.getHumanCostRecord();

        //这里的上传时间做判断，如果前端请求没有指定，使用当前时间
        Date uploadTime = viewCost.getUploadTime() == null ? new Date() : viewCost.getUploadTime();


        if (lastOne != null) {
            // 如果上传时间未超过24小时
            if (!isMoreThan24Hours(lastOne.getUploadTime(), uploadTime)) {
                return false;
            }
        }

        Integer projectId = viewCost.getProjectId();
        Integer momentId = viewCost.getMomentId();
        Integer userId = viewCost.getUploadFrom();

        User user = userMapper.selectByPrimaryKey(userId);

        List<ExtraCost> extraCosts = JSON.parseArray(extraCostListStrings, ExtraCost.class);
        for (int i = 0; i < extraCosts.size(); i++) {
            ExtraCost tempCost = extraCosts.get(i);

            tempCost.setUploadFrom(userId);

            tempCost.setUploadTime(uploadTime);
            tempCost.setProjectId(projectId);
            tempCost.setMomentId(momentId);
            short state = 1;  //不需要审核，这里直接将状态设置为正常
            tempCost.setApplyState(state);

            tempCost.setUploaderName(user.getUserRealname());
            tempCost.setWorkCode(user.getWorkCode());

            extraCostMapper.insertSelective(tempCost);

            //更新阶段成本
            MomentCost query = new MomentCost();
            query.setProjectId(tempCost.getProjectId());
            query.setMomentId(tempCost.getMomentId());
            MomentCost momentCost = momentCostMapper.selectByProjectIdAndMomentId(query);
            momentCost.setRealCost(momentCost.getRealCost().add(tempCost.getAmount()));
            int rowInMomentCost = momentCostMapper.updateByPrimaryKey(momentCost);

            //更新项目成本
            ProjectCost projectCost = projectCostMapper.selectByProjectId(tempCost.getProjectId());
            projectCost.setRealCost(projectCost.getRealCost().add(tempCost.getAmount()));
            int rowInProjectCost = projectCostMapper.updateByPrimaryKey(projectCost);
        }

        humanCostRecord.setProjectId(projectId);
        humanCostRecord.setMomentId(momentId);
        humanCostRecord.setUploadTime(uploadTime);
        humanCostRecord.setUploadFrom(userId);
        humanCostRecord.setUploaderName(user.getUserRealname());
        humanCostRecord.setWorkCode(user.getWorkCode());
        short state = 1;
        humanCostRecord.setApplyState(state);
        humanCostRecordMapper.insertSelective(humanCostRecord);

        //更新阶段成本
        MomentCost query = new MomentCost();
        query.setProjectId(humanCostRecord.getProjectId());
        query.setMomentId(humanCostRecord.getMomentId());
        MomentCost momentCost = momentCostMapper.selectByProjectIdAndMomentId(query);
        momentCost.setRealCost(momentCost.getRealCost().add(roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay().multiply(new BigDecimal(humanCostRecord.getWorkTime()))));
        momentCostMapper.updateByPrimaryKeySelective(momentCost);

        //更新项目成本
        ProjectCost projectCost = projectCostMapper.selectByProjectId(humanCostRecord.getProjectId());
        projectCost.setRealCost(projectCost.getRealCost().add(roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay().multiply(new BigDecimal(humanCostRecord.getWorkTime()))));
        projectCostMapper.updateByPrimaryKeySelective(projectCost);

        //执行到这里，成功
        return true;
    }

    @Override
    public Role getRoleById(Integer id) {
        return roleMapper.selectRoleById(id);
    }

    private boolean isMoreThan24Hours(Date oldDate, Date newDate) {
        long nh = 1000 * 60 * 60; // 一小时毫秒数
        long diff = newDate.getTime() - oldDate.getTime(); // 两个日期毫秒差
        long hours = diff / nh;
        if (hours > 24) {
            return true;
        }
        return false;
    }
}
