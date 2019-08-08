package com.supervision.project.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.document.mapper.DocumentMapper;
import com.supervision.document.model.Document;
import com.supervision.project.ViewModel.ViewDailyAndHumanCost;
import com.supervision.project.mapper.*;
import com.supervision.project.model.*;
import com.supervision.project.service.ProjectService;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:project service impl
 * @Author:TjSanshao
 * @Create:2019-01-30 13:42
 *
 **/
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private ProjectTeamMapper projectTeamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectTypeMapper projectTypeMapper;

    @Autowired
    private ProjectCostMapper projectCostMapper;

    @Autowired
    private MomentCostMapper momentCostMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @Autowired
    private HumanCostRecordMapper humanCostRecordMapper;

    @Autowired
    private ProjectProcessMapper projectProcessMapper;

    @Override
    @Transactional
    @Async
    public int addProject(Project project) {
        project.setCreateTime(new Date());

        short projectState = 0;  //0表示未开始
        project.setProjectState(projectState);

        if (project.getNotResidentWeekDays() == null) {
            short days = 7;
            project.setNotResidentWeekDays(days);
        }
        if (project.getNotResidentWeekDays().intValue() > 7 || project.getNotResidentWeekDays().intValue() < 1) {
            short days = 7;
            project.setNotResidentWeekDays(days);
        }

        project.setBuildName(userMapper.selectByPrimaryKey(project.getCreateFrom()).getUserRealname());

        int row = projectMapper.insertSelective(project);

        //项目的类型
        short projectType = project.getProjectType();

        //新建的project的Id
        Integer projectId = project.getId();

        //将项目总监以及总监代表加入到项目团队并设置团队角色
        ProjectTeam director = new ProjectTeam();
        director.setProjectId(projectId);
        director.setUserId(project.getProjectDirector());
        short roleInProjectDirector = 2;  //2表示总监
        director.setRoleInProject(roleInProjectDirector);

        ProjectTeam delegate = new ProjectTeam();
        delegate.setProjectId(projectId);
        delegate.setUserId(project.getDelegate());
        short roleInProjectDelegate = 1;  //1表示总监代表
        delegate.setRoleInProject(roleInProjectDelegate);

        projectTeamMapper.insertSelective(director);
        projectTeamMapper.insertSelective(delegate);

        //创建项目成本
        ProjectCost projectCost = new ProjectCost();
        projectCost.setProjectId(projectId);
        projectCost.setExpectCost(new BigDecimal(0));  //这里暂时没有好的初始化值
        projectCost.setRealCost(new BigDecimal(0));
        projectCostMapper.insertSelective(projectCost);

        // 创建项目进度
        ProjectProcess process = new ProjectProcess();
        process.setProjectId(projectId);
        process.setProjectProcess(0);
        projectProcessMapper.insertSelective(process);

        //0表示软件类监理
        if (projectType == 0) {

            //构建项目阶段
            Moment preSaleTechnologySupportMoment = preSaleTechnologySupportMomentBuildSoftware(projectId);
            Moment preImplementMoment = preImplementMomentBuildSoftware(projectId);
            Moment implementMoment = implementMomentBuildSoftware(projectId);
            Moment checkMoment = checkMomentBuildSoftware(projectId);
            Moment accountsMoment = accountsMomentBuildSoftware(projectId);

            momentMapper.insertSelective(preSaleTechnologySupportMoment);
            momentMapper.insertSelective(preImplementMoment);
            momentMapper.insertSelective(implementMoment);
            momentMapper.insertSelective(checkMoment);
            momentMapper.insertSelective(accountsMoment);

            MomentCost preSaleTechnologySupportMomentCost = new MomentCost();
            preSaleTechnologySupportMomentCost.setProjectId(projectId);
            preSaleTechnologySupportMomentCost.setMomentId(preSaleTechnologySupportMoment.getId());
            preSaleTechnologySupportMomentCost.setExpectCost(new BigDecimal(0));
            preSaleTechnologySupportMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(preSaleTechnologySupportMomentCost);

            MomentCost preImplementMomentCost = new MomentCost();
            preImplementMomentCost.setProjectId(projectId);
            preImplementMomentCost.setMomentId(preImplementMoment.getId());
            preImplementMomentCost.setExpectCost(new BigDecimal(0));
            preImplementMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(preImplementMomentCost);

            MomentCost implementMomentCost = new MomentCost();
            implementMomentCost.setProjectId(projectId);
            implementMomentCost.setMomentId(implementMoment.getId());
            implementMomentCost.setExpectCost(new BigDecimal(0));
            implementMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(implementMomentCost);

            MomentCost checkMomentCost = new MomentCost();
            checkMomentCost.setProjectId(projectId);
            checkMomentCost.setMomentId(checkMoment.getId());
            checkMomentCost.setExpectCost(new BigDecimal(0));
            checkMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(checkMomentCost);

            MomentCost accountsMomentCost = new MomentCost();
            accountsMomentCost.setProjectId(projectId);
            accountsMomentCost.setMomentId(accountsMoment.getId());
            accountsMomentCost.setExpectCost(new BigDecimal(0));
            accountsMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(accountsMomentCost);

            List<Document> preSaleDocs = preSaleTechnologySupportDocBuildSoftware(projectId, preSaleTechnologySupportMoment.getId());
            for (int i = 0; i < preSaleDocs.size(); i++) {
                documentMapper.insertSelective(preSaleDocs.get(i));
            }

            List<Document> preImplDocs = preImplementDocBuildSoftware(projectId, preImplementMoment.getId());
            for (int i = 0; i < preImplDocs.size(); i++) {
                documentMapper.insertSelective(preImplDocs.get(i));
            }

            List<Document> implDocs = implementDocBuildSoftware(projectId, implementMoment.getId());
            for (int i = 0; i < implDocs.size(); i++) {
                documentMapper.insertSelective(implDocs.get(i));
            }

            List<Document> checkDocs = checkDocBuildSoftware(projectId, checkMoment.getId());
            for (int i = 0; i < checkDocs.size(); i++) {
                documentMapper.insertSelective(checkDocs.get(i));
            }

        }
        // 除了其他，软件
        else {
            //构建项目阶段
            Moment preSaleTechnologySupportMoment = preSaleTechnologySupportMomentBuild(projectId);
            Moment preImplementMoment = preImplementMomentBuild(projectId);
            Moment implementMoment = implementMomentBuild(projectId);
            Moment checkMoment = checkMomentBuild(projectId);
            Moment accountsMoment = accountsMomentBuild(projectId);
            Moment devOpsMoment = devOpsMomentBuild(projectId);

            momentMapper.insertSelective(preSaleTechnologySupportMoment);
            momentMapper.insertSelective(preImplementMoment);
            momentMapper.insertSelective(implementMoment);
            momentMapper.insertSelective(checkMoment);
            momentMapper.insertSelective(accountsMoment);
            momentMapper.insertSelective(devOpsMoment);

            //构建阶段成本
            MomentCost preSaleTechnologySupportMomentCost = new MomentCost();
            preSaleTechnologySupportMomentCost.setProjectId(projectId);
            preSaleTechnologySupportMomentCost.setMomentId(preSaleTechnologySupportMoment.getId());
            preSaleTechnologySupportMomentCost.setExpectCost(new BigDecimal(0));
            preSaleTechnologySupportMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(preSaleTechnologySupportMomentCost);

            MomentCost preImplementMomentCost = new MomentCost();
            preImplementMomentCost.setProjectId(projectId);
            preImplementMomentCost.setMomentId(preImplementMoment.getId());
            preImplementMomentCost.setExpectCost(new BigDecimal(0));
            preImplementMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(preImplementMomentCost);

            MomentCost implementMomentCost = new MomentCost();
            implementMomentCost.setProjectId(projectId);
            implementMomentCost.setMomentId(implementMoment.getId());
            implementMomentCost.setExpectCost(new BigDecimal(0));
            implementMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(implementMomentCost);

            MomentCost checkMomentCost = new MomentCost();
            checkMomentCost.setProjectId(projectId);
            checkMomentCost.setMomentId(checkMoment.getId());
            checkMomentCost.setExpectCost(new BigDecimal(0));
            checkMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(checkMomentCost);

            MomentCost accountsMomentCost = new MomentCost();
            accountsMomentCost.setProjectId(projectId);
            accountsMomentCost.setMomentId(accountsMoment.getId());
            accountsMomentCost.setExpectCost(new BigDecimal(0));
            accountsMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(accountsMomentCost);

            MomentCost devOpsMomentCost = new MomentCost();
            devOpsMomentCost.setProjectId(projectId);
            devOpsMomentCost.setMomentId(devOpsMoment.getId());
            devOpsMomentCost.setExpectCost(new BigDecimal(0));
            devOpsMomentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(devOpsMomentCost);

            List<Document> preSaleDocs = preSaleTechnologySupportDocBuild(projectId, preSaleTechnologySupportMoment.getId());
            for (int i = 0; i < preSaleDocs.size(); i++) {
                documentMapper.insertSelective(preSaleDocs.get(i));
            }

            List<Document> preImplDocs = preImplementDocBuild(projectId, preImplementMoment.getId());
            for (int i = 0; i < preImplDocs.size(); i++) {
                documentMapper.insertSelective(preImplDocs.get(i));
            }

            List<Document> implDocs = implementDocBuild(projectId, implementMoment.getId());
            for (int i = 0; i < implDocs.size(); i++) {
                documentMapper.insertSelective(implDocs.get(i));
            }

            List<Document> checkDocs = checkDocBuild(projectId, checkMoment.getId());
            for (int i = 0; i < checkDocs.size(); i++) {
                documentMapper.insertSelective(checkDocs.get(i));
            }

            List<Document> devOpsDocs = devOpsDocBuild(projectId, devOpsMoment.getId());
            for (int i = 0; i < devOpsDocs.size(); i++) {
                documentMapper.insertSelective(devOpsDocs.get(i));
            }
        }

        return row;
    }

    @Override
    @Transactional  //开启事务，这里会使用异步的方式构建项目，因此在出错后只会回滚，但是用户本身并不知晓项目创建失败
    @Async  //开启异步
    public boolean addCustomProject(CustomProject customProject) {

        Project project = new Project();
        project.setCreateFrom(customProject.getCreateFrom());
        project.setProjectName(customProject.getProjectName());
        project.setProjectCode(customProject.getProjectCode());
        project.setProjectDirector(customProject.getProjectDirector());
        project.setDelegate(customProject.getDelegate());
        project.setDepartment(customProject.getDepartment());
        project.setProjectType(customProject.getProjectType());
        project.setProjectDesc(customProject.getProjectDesc());
        project.setCreateTime(new Date());
        project.setExpectFinishTime(customProject.getExpectFinishTime());
        short projectState = 0;
        project.setProjectState(projectState);
        project.setBuildName(userMapper.selectByPrimaryKey(project.getCreateFrom()).getUserRealname());
        project.setClientContact(customProject.getClientContact());
        project.setPersonCharge(customProject.getPersonCharge());
        project.setAgreementTime(customProject.getAgreementTime());
        project.setBuildDuration(customProject.getBuildDuration());
        project.setServiceDuration(customProject.getServiceDuration());
        project.setIsResident(customProject.getIsResident());
        project.setProjectBudget(customProject.getProjectBudget());
        project.setSupervisionAmount(customProject.getSupervisionAmount());
        project.setSupervisionPaid(customProject.getSupervisionPaid());
        project.setNotResidentWeekDays(customProject.getNotResidentWeekDays());
        if (project.getNotResidentWeekDays() == null) {
            short days = 7;
            project.setNotResidentWeekDays(days);
        }
        if (project.getNotResidentWeekDays().intValue() > 7 || project.getNotResidentWeekDays().intValue() < 1) {
            short days = 7;
            project.setNotResidentWeekDays(days);
        }

        int rowInProject = projectMapper.insertSelective(project);

        //得到插入记录的主键Id
        Integer projectId = project.getId();

        //将项目总监以及总监代表加入到项目团队并设置团队角色
        ProjectTeam director = new ProjectTeam();
        director.setProjectId(projectId);
        director.setUserId(project.getProjectDirector());
        short roleInProjectDirector = 2;  //2表示总监
        director.setRoleInProject(roleInProjectDirector);

        ProjectTeam delegate = new ProjectTeam();
        delegate.setProjectId(projectId);
        delegate.setUserId(project.getDelegate());
        short roleInProjectDelegate = 1;  //1表示总监代表
        delegate.setRoleInProject(roleInProjectDelegate);

        projectTeamMapper.insertSelective(director);
        projectTeamMapper.insertSelective(delegate);

        //构建项目成本
        ProjectCost projectCost = new ProjectCost();
        projectCost.setProjectId(projectId);
        projectCost.setExpectCost(new BigDecimal(0));  //这里暂时没有好的初始化值
        projectCost.setRealCost(new BigDecimal(0));
        projectCostMapper.insertSelective(projectCost);

        // 创建项目进度
        ProjectProcess process = new ProjectProcess();
        process.setProjectId(projectId);
        process.setProjectProcess(0);
        projectProcessMapper.insertSelective(process);

        List<CustomMoment> momentsUploaded = JSON.parseArray(customProject.getMoments(), CustomMoment.class);

        //开始构建项目阶段
        for (int i = 0; i < momentsUploaded.size(); i++) {
            //取出CustomMoment，将属性封装到Moment里面
            CustomMoment customMoment = momentsUploaded.get(i);
            Moment moment = new Moment();

            //上传的文档
            List<Document> docsUploaded = customMoment.getDocuments();

            moment.setProjectId(projectId);
            moment.setMomentOrder(customMoment.getMomentOrder() + 1);
            //假定moment都设置了momentOrder
            if (moment.getMomentOrder() == null) {
                return false;
            }
            moment.setMomentDesc(customMoment.getMomentDesc());
            short momentState = 0;
            moment.setState(momentState);
            moment.setUploadedFileNumber(0);
            //设置阶段的总文档数目
            moment.setFileNumber(customMoment.getDocuments().size());
            moment.setExpectFinishTime(customMoment.getExpectFinishTime());
            moment.setMomentName(customMoment.getMomentName());
            moment.setExpectFinishTime(customMoment.getExpectFinishTime());
            momentMapper.insertSelective(moment);

            //构建阶段成本
            MomentCost momentCost = new MomentCost();
            momentCost.setProjectId(projectId);
            momentCost.setMomentId(moment.getId());
            momentCost.setExpectCost(new BigDecimal(0));
            momentCost.setRealCost(new BigDecimal(0));
            momentCostMapper.insertSelective(momentCost);

            for (int j = 0; j < docsUploaded.size(); j++) {

                Document document = docsUploaded.get(j);

                document.setProjectId(projectId);
                document.setMomentId(moment.getId());
                short state = 0;
                document.setDocState(state);

                documentMapper.insertSelective(document);

            }
        }

        if (rowInProject < 1) {
            return false;
        }

        return true;
    }

    @Transactional
    @Override
    public boolean addCustomMoment(CustomMoment customMoment) {

        List<Document> documents = JSON.parseArray(customMoment.getDocumentStrings(), Document.class);
        customMoment.setDocuments(documents);

        Moment moment = new Moment();
        moment.setProjectId(customMoment.getProjectId());

        // 设置项目阶段在项目中的顺序
        List<Moment> moments = momentMapper.selectAllByProjectId(moment.getProjectId());
        if (customMoment.getMomentOrder() == null) {
            // 如果没有指定顺序，那么默认为最后一个
            moment.setMomentOrder(moments.size() + 1);
        }
        // 如果指定了
        moment.setMomentOrder(customMoment.getMomentOrder());
        for (int i = 0; i < moments.size(); i++) {
            Moment tempMoment = moments.get(i);
            if (tempMoment.getMomentOrder() >= moment.getMomentOrder()) {
                tempMoment.setMomentOrder(tempMoment.getMomentOrder() + 1);
                momentMapper.updateByPrimaryKeySelective(tempMoment);
            }
        }

        moment.setMomentDesc(customMoment.getMomentDesc());
        moment.setState(moments.get(0).getState());
        moment.setFileNumber(documents.size());
        moment.setUploadedFileNumber(0);
        if (moment.getState().intValue() == 1) {
            moment.setStartTime(new Date());
        }
        moment.setExpectFinishTime(customMoment.getExpectFinishTime());
        moment.setMomentName(customMoment.getMomentName());

        momentMapper.insertSelective(moment);

        // 构建项目阶段成本
        MomentCost momentCost = new MomentCost();
        momentCost.setProjectId(moment.getProjectId());
        momentCost.setMomentId(moment.getId());
        momentCost.setExpectCost(new BigDecimal(0));
        momentCost.setRealCost(new BigDecimal(0));
        momentCostMapper.insertSelective(momentCost);

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);

            document.setProjectId(moment.getProjectId());
            document.setMomentId(moment.getId());
            short state = 0;
            document.setDocState(state);
            document.setIsTemplate(1);
            document.setOrderMoment(i + 1);

            documentMapper.insertSelective(document);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean addDailyAndHumanCost(ViewDailyAndHumanCost viewDailyAndHumanCost, Integer uploadFrom) {

        List<Daily> dailyList = JSON.parseArray(viewDailyAndHumanCost.getDailyList(), Daily.class);
        List<HumanCostRecord> costRecordList = JSON.parseArray(viewDailyAndHumanCost.getCostRecordList(), HumanCostRecord.class);
        Date uploadTime = viewDailyAndHumanCost.getUploadTime();

        //这里使用统一的上传时间
        if (uploadTime == null) {
            uploadTime = new Date();
        }

        for (int i = 0; i < dailyList.size(); i++) {
            Daily daily = dailyList.get(i);
            daily.setUploadFrom(uploadFrom);
            daily.setUploadTime(uploadTime);
            dailyMapper.insertSelective(daily);
        }

        for (int i = 0; i < costRecordList.size(); i++) {
            HumanCostRecord humanCostRecord = costRecordList.get(i);
            humanCostRecord.setUploadFrom(uploadFrom);
            humanCostRecord.setUploadTime(uploadTime);
            short state = 1;
            humanCostRecord.setApplyState(state);
            User user = userMapper.selectByPrimaryKey(uploadFrom);
            humanCostRecord.setUploaderName(user.getUserRealname());
            humanCostRecord.setWorkCode(user.getWorkCode());
            if (humanCostRecord.getMomentId() == null) {
                //这里判断是否填写了momentId，如果没有填写，抛出异常，数据库回滚
                throw new NullPointerException("MomentId Can not be null!");
            }
            humanCostRecordMapper.insertSelective(humanCostRecord);
        }

        return true;
    }

    //构建项目售前技术支持阶段（软件）
    private Moment preSaleTechnologySupportMomentBuildSoftware(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(1);

        moment.setMomentDesc("项目售前技术支持阶段");
        moment.setMomentName("项目售前技术支持阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(5);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> preSaleTechnologySupportDocBuildSoftware(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docCost = new Document();
        docCost.setDocName("项目成本预算表v1.0");
        docCost.setProjectId(projectId);
        docCost.setMomentId(momentId);
        docCost.setDocType("项目成本预算表v1.0");
        docCost.setDocState(state);
        docCost.setIsTemplate(1);
        docCost.setOrderMoment(1);

        Document docD5 = new Document();
        docD5.setDocName("会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(2);

        Document docTeamBuild = new Document();
        docTeamBuild.setDocName("团队组建通知书");
        docTeamBuild.setProjectId(projectId);
        docTeamBuild.setMomentId(momentId);
        docTeamBuild.setDocType("团队组建通知书");
        docTeamBuild.setDocState(state);
        docTeamBuild.setIsTemplate(1);
        docTeamBuild.setOrderMoment(3);

        Document docCost2 = new Document();
        docCost2.setDocName("项目成本预算表v2.0");
        docCost2.setProjectId(projectId);
        docCost2.setMomentId(momentId);
        docCost2.setDocType("项目成本预算表v2.0");
        docCost2.setDocState(state);
        docCost2.setIsTemplate(1);
        docCost2.setOrderMoment(4);

        Document docC4 = new Document();
        docC4.setDocName("文件评审表");
        docC4.setProjectId(projectId);
        docC4.setMomentId(momentId);
        docC4.setDocType("C4");
        docC4.setDocState(state);
        docC4.setIsTemplate(1);
        docC4.setOrderMoment(5);

        documents.add(docCost);
        documents.add(docD5);
        documents.add(docTeamBuild);
        documents.add(docCost2);
        documents.add(docC4);

        return documents;
    }

    //构建项目实施准备阶段（软件）
    private Moment preImplementMomentBuildSoftware(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(2);

        moment.setMomentDesc("项目实施准备阶段");
        moment.setMomentName("项目实施准备阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(7);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> preImplementDocBuildSoftware(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docD1 = new Document();
        docD1.setDocName("监理规划书");
        docD1.setProjectId(projectId);
        docD1.setMomentId(momentId);
        docD1.setDocType("D1");
        docD1.setDocState(state);
        docD1.setIsTemplate(1);
        docD1.setOrderMoment(1);

        Document docD2 = new Document();
        docD2.setDocName("监理细则");
        docD2.setProjectId(projectId);
        docD2.setMomentId(momentId);
        docD2.setDocType("D2");
        docD2.setDocState(state);
        docD2.setIsTemplate(1);
        docD2.setOrderMoment(2);

        Document docImplementSolution = new Document();
        docImplementSolution.setDocName("监理实施方案");
        docImplementSolution.setProjectId(projectId);
        docImplementSolution.setMomentId(momentId);
        docImplementSolution.setDocType("监理实施方案");
        docImplementSolution.setDocState(state);
        docImplementSolution.setIsTemplate(1);
        docImplementSolution.setOrderMoment(3);

        Document docWork = new Document();
        docWork.setDocName("监理工作范围说明书");
        docWork.setProjectId(projectId);
        docWork.setMomentId(momentId);
        docWork.setDocType("监理工作范围说明书");
        docWork.setDocState(state);
        docWork.setIsTemplate(1);
        docWork.setOrderMoment(4);

        Document docD5 = new Document();
        docD5.setDocName("项目启动会议会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(5);

        Document docWBS = new Document();
        docWBS.setDocName("项目WBS任务分解表");
        docWBS.setProjectId(projectId);
        docWBS.setMomentId(momentId);
        docWBS.setDocType("WBS");
        docWBS.setDocState(state);
        docWBS.setIsTemplate(1);
        docWBS.setOrderMoment(6);

        Document docB3 = new Document();
        docB3.setDocName("工程开工令");
        docB3.setProjectId(projectId);
        docB3.setMomentId(momentId);
        docB3.setDocType("B3");
        docB3.setDocState(state);
        docB3.setIsTemplate(1);
        docB3.setOrderMoment(7);

        documents.add(docD1);
        documents.add(docD2);
        documents.add(docImplementSolution);
        documents.add(docWork);
        documents.add(docD5);
        documents.add(docWBS);
        documents.add(docB3);

        return documents;
    }

    //构建项目实施阶段（软件）
    private Moment implementMomentBuildSoftware(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(3);

        moment.setMomentDesc("项目实施阶段");
        moment.setMomentName("项目实施阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(9);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> implementDocBuildSoftware(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docC1 = new Document();
        docC1.setDocName("工作联系单");
        docC1.setProjectId(projectId);
        docC1.setMomentId(momentId);
        docC1.setDocType("C1");
        docC1.setDocState(state);
        docC1.setIsTemplate(1);
        docC1.setOrderMoment(1);

        Document docB1 = new Document();
        docB1.setDocName("监理通知单");
        docB1.setProjectId(projectId);
        docB1.setMomentId(momentId);
        docB1.setDocType("B1");
        docB1.setDocState(state);
        docB1.setIsTemplate(1);
        docB1.setOrderMoment(2);

        Document docB8 = new Document();
        docB8.setDocName("整改通知单");
        docB8.setProjectId(projectId);
        docB8.setMomentId(momentId);
        docB8.setDocType("B8");
        docB8.setDocState(state);
        docB8.setIsTemplate(1);
        docB8.setOrderMoment(3);

        Document docB10 = new Document();
        docB10.setDocName("监理罚款单");
        docB10.setProjectId(projectId);
        docB10.setMomentId(momentId);
        docB10.setDocType("B10");
        docB10.setDocState(state);
        docB10.setIsTemplate(1);
        docB10.setOrderMoment(4);

        Document docD5 = new Document();
        docD5.setDocName("会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(5);

        Document docFileCheck = new Document();
        docFileCheck.setDocName("文件评审表");
        docFileCheck.setProjectId(projectId);
        docFileCheck.setMomentId(momentId);
        docFileCheck.setDocType("文件评审表");
        docFileCheck.setDocState(state);
        docFileCheck.setIsTemplate(1);
        docFileCheck.setOrderMoment(6);

        Document docB4 = new Document();
        docB4.setDocName("工程支付证书");
        docB4.setProjectId(projectId);
        docB4.setMomentId(momentId);
        docB4.setDocType("B4");
        docB4.setDocState(state);
        docB4.setIsTemplate(1);
        docB4.setOrderMoment(7);

        Document docTestReport = new Document();
        docTestReport.setDocName("监理测试报告");
        docTestReport.setProjectId(projectId);
        docTestReport.setMomentId(momentId);
        docTestReport.setDocType("监理测试报告");
        docTestReport.setDocState(state);
        docTestReport.setIsTemplate(1);
        docTestReport.setOrderMoment(8);

        Document docD11 = new Document();
        docD11.setDocName("阶段总结报告");
        docD11.setProjectId(projectId);
        docD11.setMomentId(momentId);
        docD11.setDocType("D11");
        docD11.setDocState(state);
        docD11.setIsTemplate(1);
        docD11.setOrderMoment(9);

        documents.add(docC1);
        documents.add(docB1);
        documents.add(docB8);
        documents.add(docB10);
        documents.add(docD5);
        documents.add(docFileCheck);
        documents.add(docB4);
        documents.add(docTestReport);
        documents.add(docD11);

        return documents;
    }

    //构建项目验收阶段（软件）
    private Moment checkMomentBuildSoftware(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(4);

        moment.setMomentDesc("项目验收阶段");
        moment.setMomentName("项目验收阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(4);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> checkDocBuildSoftware(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docTestReport = new Document();
        docTestReport.setDocName("监理测试报告（回归测试）");
        docTestReport.setProjectId(projectId);
        docTestReport.setMomentId(momentId);
        docTestReport.setDocType("监理测试报告");
        docTestReport.setDocState(state);
        docTestReport.setIsTemplate(1);
        docTestReport.setOrderMoment(1);

        Document docD5 = new Document();
        docD5.setDocName("初验验收会议会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(2);

        Document docMeetCheck = new Document();
        docMeetCheck.setDocName("会议签到表");
        docMeetCheck.setProjectId(projectId);
        docMeetCheck.setMomentId(momentId);
        docMeetCheck.setDocType("会议签到表");
        docMeetCheck.setDocState(state);
        docMeetCheck.setIsTemplate(1);
        docMeetCheck.setOrderMoment(3);

        Document docB8 = new Document();
        docB8.setDocName("初验整改通知单");
        docB8.setProjectId(projectId);
        docB8.setMomentId(momentId);
        docB8.setDocType("B8");
        docB8.setDocState(state);
        docB8.setIsTemplate(1);
        docB8.setOrderMoment(3);

        documents.add(docTestReport);
        documents.add(docD5);
        documents.add(docMeetCheck);
        documents.add(docB8);

        return documents;
    }

    //构建项目结算阶段（软件）
    private Moment accountsMomentBuildSoftware(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(5);

        moment.setMomentDesc("项目结算阶段");
        moment.setMomentName("项目结算阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(2);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }


    //构建项目售前技术支持阶段（集成、工程）
    private Moment preSaleTechnologySupportMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(1);

        moment.setMomentDesc("项目售前技术支持阶段");
        moment.setMomentName("项目售前技术支持阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(5);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> preSaleTechnologySupportDocBuild(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docCost = new Document();
        docCost.setDocName("项目成本预算表v1.0");
        docCost.setProjectId(projectId);
        docCost.setMomentId(momentId);
        docCost.setDocType("项目成本预算表v1.0");
        docCost.setDocState(state);
        docCost.setIsTemplate(1);
        docCost.setOrderMoment(1);

        Document docD5 = new Document();
        docD5.setDocName("会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(2);

        Document docTeamBuild = new Document();
        docTeamBuild.setDocName("团队组建通知书");
        docTeamBuild.setProjectId(projectId);
        docTeamBuild.setMomentId(momentId);
        docTeamBuild.setDocType("团队组建通知书");
        docTeamBuild.setDocState(state);
        docTeamBuild.setIsTemplate(1);
        docTeamBuild.setOrderMoment(3);

        Document docCost2 = new Document();
        docCost2.setDocName("项目成本预算表v2.0");
        docCost2.setProjectId(projectId);
        docCost2.setMomentId(momentId);
        docCost2.setDocType("项目成本预算表v2.0");
        docCost2.setDocState(state);
        docCost2.setIsTemplate(1);
        docCost2.setOrderMoment(4);

        Document docC4 = new Document();
        docC4.setDocName("文件评审表");
        docC4.setProjectId(projectId);
        docC4.setMomentId(momentId);
        docC4.setDocType("C4");
        docC4.setDocState(state);
        docC4.setIsTemplate(1);
        docC4.setOrderMoment(5);

        documents.add(docCost);
        documents.add(docD5);
        documents.add(docTeamBuild);
        documents.add(docCost2);
        documents.add(docC4);

        return documents;
    }

    //构建项目实施准备阶段（集成、工程）
    private Moment preImplementMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(2);

        moment.setMomentDesc("项目实施准备阶段");
        moment.setMomentName("项目实施准备阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(7);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> preImplementDocBuild(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docD1 = new Document();
        docD1.setDocName("监理规划书");
        docD1.setProjectId(projectId);
        docD1.setMomentId(momentId);
        docD1.setDocType("D1");
        docD1.setDocState(state);
        docD1.setIsTemplate(1);
        docD1.setOrderMoment(1);

        Document docD2 = new Document();
        docD2.setDocName("监理细则");
        docD2.setProjectId(projectId);
        docD2.setMomentId(momentId);
        docD2.setDocType("D2");
        docD2.setDocState(state);
        docD2.setIsTemplate(1);
        docD2.setOrderMoment(2);

        Document docImplementSolution = new Document();
        docImplementSolution.setDocName("监理实施方案");
        docImplementSolution.setProjectId(projectId);
        docImplementSolution.setMomentId(momentId);
        docImplementSolution.setDocType("监理实施方案");
        docImplementSolution.setDocState(state);
        docImplementSolution.setIsTemplate(1);
        docImplementSolution.setOrderMoment(3);

        Document docWork = new Document();
        docWork.setDocName("监理工作范围说明书");
        docWork.setProjectId(projectId);
        docWork.setMomentId(momentId);
        docWork.setDocType("监理工作范围说明书");
        docWork.setDocState(state);
        docWork.setIsTemplate(1);
        docWork.setOrderMoment(4);

        Document docD5 = new Document();
        docD5.setDocName("项目启动会议会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(5);

        Document docWBS = new Document();
        docWBS.setDocName("项目WBS任务分解表");
        docWBS.setProjectId(projectId);
        docWBS.setMomentId(momentId);
        docWBS.setDocType("WBS");
        docWBS.setDocState(state);
        docWBS.setIsTemplate(1);
        docWBS.setOrderMoment(6);

        Document docB3 = new Document();
        docB3.setDocName("工程开工令");
        docB3.setProjectId(projectId);
        docB3.setMomentId(momentId);
        docB3.setDocType("B3");
        docB3.setDocState(state);
        docB3.setIsTemplate(1);
        docB3.setOrderMoment(7);

        documents.add(docD1);
        documents.add(docD2);
        documents.add(docImplementSolution);
        documents.add(docWork);
        documents.add(docD5);
        documents.add(docWBS);
        documents.add(docB3);

        return documents;
    }

    //构建项目实施阶段（集成、工程）
    private Moment implementMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(3);

        moment.setMomentDesc("项目实施阶段");
        moment.setMomentName("项目实施阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(9);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> implementDocBuild(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docC1 = new Document();
        docC1.setDocName("工作联系单");
        docC1.setProjectId(projectId);
        docC1.setMomentId(momentId);
        docC1.setDocType("C1");
        docC1.setDocState(state);
        docC1.setIsTemplate(1);
        docC1.setOrderMoment(1);

        Document docB1 = new Document();
        docB1.setDocName("监理通知单");
        docB1.setProjectId(projectId);
        docB1.setMomentId(momentId);
        docB1.setDocType("B1");
        docB1.setDocState(state);
        docB1.setIsTemplate(1);
        docB1.setOrderMoment(2);

        Document docB8 = new Document();
        docB8.setDocName("整改通知单");
        docB8.setProjectId(projectId);
        docB8.setMomentId(momentId);
        docB8.setDocType("B8");
        docB8.setDocState(state);
        docB8.setIsTemplate(1);
        docB8.setOrderMoment(3);

        Document docB10 = new Document();
        docB10.setDocName("监理罚款单");
        docB10.setProjectId(projectId);
        docB10.setMomentId(momentId);
        docB10.setDocType("B10");
        docB10.setDocState(state);
        docB10.setIsTemplate(1);
        docB10.setOrderMoment(4);

        Document docD5 = new Document();
        docD5.setDocName("会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(5);

          //在给出的流程中，集成、工程类监理不需要在实施阶段提交该文档
//        Document docFileCheck = new Document();
//        docFileCheck.setDocName("文件评审表");
//        docFileCheck.setProjectId(projectId);
//        docFileCheck.setMomentId(momentId);
//        docFileCheck.setDocType("文件评审表");
//        docFileCheck.setDocState(state);

        Document docB4 = new Document();
        docB4.setDocName("工程支付证书");
        docB4.setProjectId(projectId);
        docB4.setMomentId(momentId);
        docB4.setDocType("B4");
        docB4.setDocState(state);
        docB4.setIsTemplate(1);
        docB4.setOrderMoment(6);

        Document docTestReport = new Document();
        docTestReport.setDocName("监理测试报告");
        docTestReport.setProjectId(projectId);
        docTestReport.setMomentId(momentId);
        docTestReport.setDocType("监理测试报告");
        docTestReport.setDocState(state);
        docTestReport.setIsTemplate(1);
        docTestReport.setOrderMoment(7);

        Document docD8 = new Document();
        docD8.setDocName("现场巡查报告");
        docD8.setProjectId(projectId);
        docD8.setMomentId(momentId);
        docD8.setDocType("D8");
        docD8.setDocState(state);
        docD8.setIsTemplate(1);
        docD8.setOrderMoment(8);

        Document docD11 = new Document();
        docD11.setDocName("阶段总结报告");
        docD11.setProjectId(projectId);
        docD11.setMomentId(momentId);
        docD11.setDocType("D11");
        docD11.setDocState(state);
        docD11.setIsTemplate(1);
        docD11.setOrderMoment(9);

        documents.add(docC1);
        documents.add(docB1);
        documents.add(docB8);
        documents.add(docB10);
        documents.add(docD5);
        documents.add(docB4);
        documents.add(docTestReport);
        documents.add(docD11);
        documents.add(docD8);

        return documents;
    }

    //构建项目验收阶段（集成、工程）
    private Moment checkMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(4);

        moment.setMomentDesc("项目验收阶段");
        moment.setMomentName("项目验收阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(3);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> checkDocBuild(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docD5 = new Document();
        docD5.setDocName("初验验收会议会议纪要");
        docD5.setProjectId(projectId);
        docD5.setMomentId(momentId);
        docD5.setDocType("D5");
        docD5.setDocState(state);
        docD5.setIsTemplate(1);
        docD5.setOrderMoment(1);

        Document docMeetCheck = new Document();
        docMeetCheck.setDocName("会议签到表");
        docMeetCheck.setProjectId(projectId);
        docMeetCheck.setMomentId(momentId);
        docMeetCheck.setDocType("会议签到表");
        docMeetCheck.setDocState(state);
        docMeetCheck.setIsTemplate(1);
        docMeetCheck.setOrderMoment(2);

        Document docB8 = new Document();
        docB8.setDocName("初验整改通知单");
        docB8.setProjectId(projectId);
        docB8.setMomentId(momentId);
        docB8.setDocType("B8");
        docB8.setDocState(state);
        docB8.setIsTemplate(1);
        docB8.setOrderMoment(3);

        documents.add(docD5);
        documents.add(docMeetCheck);
        documents.add(docB8);

        return documents;
    }

    //构建项目结算阶段（集成、工程）
    private Moment accountsMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(5);

        moment.setMomentDesc("项目结算阶段");
        moment.setMomentName("项目结算阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(0);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }

    //构建项目运维阶段（集成、工程）
    private Moment devOpsMomentBuild(int projectId) {
        Moment moment = new Moment();

        //设置项目阶段所属的项目
        moment.setProjectId(projectId);

        //设置项目阶段的状态
        short state = 0;  //0表示未开始
        moment.setState(state);

        //设置项目阶段在项目中的顺序，从1开始
        moment.setMomentOrder(6);

        moment.setMomentDesc("项目运维阶段");
        moment.setMomentName("项目运维阶段");

        //设置项目阶段所需要上传的文档数
        moment.setFileNumber(2);
        //设置已上传的文档数
        moment.setUploadedFileNumber(0);

        return moment;
    }
    private List<Document> devOpsDocBuild(int projectId, int momentId) {
        List<Document> documents = new ArrayList<>();

        short state = 0;

        Document docC1 = new Document();
        docC1.setDocName("工作联系单");
        docC1.setProjectId(projectId);
        docC1.setMomentId(momentId);
        docC1.setDocType("C1");
        docC1.setDocState(state);
        docC1.setIsTemplate(1);
        docC1.setOrderMoment(1);

        Document docB1 = new Document();
        docB1.setDocName("监理通知单");
        docB1.setProjectId(projectId);
        docB1.setMomentId(momentId);
        docB1.setDocType("B1");
        docB1.setDocState(state);
        docB1.setIsTemplate(1);
        docB1.setOrderMoment(2);

        Document docB8 = new Document();
        docB8.setDocName("整改通知单");
        docB8.setProjectId(projectId);
        docB8.setMomentId(momentId);
        docB8.setDocType("B8");
        docB8.setDocState(state);
        docB8.setIsTemplate(1);
        docB8.setOrderMoment(2);

        documents.add(docC1);
        documents.add(docB1);
        documents.add(docB8);

        return documents;
    }

    @Override
    public Project getProject(int projectId) {
        return projectMapper.selectByPrimaryKey(projectId);
    }

    @Override
    public Object getAllProjects(int page, int pageSize) {
        //查看所有的项目，这里一般用于高级权限用户查看项目列表
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAll();
        return new PageInfo<>(projects);
    }

    @Override
    public PageInfo<Project> getAllProjectsByUserWithPageInfo(int page, int pageSize, int userId) {
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByUser(userId);
        return new PageInfo<>(projects);
    }

    @Override
    public Object getAllProjectsByUser(int page, int pageSize, Integer userId) {

        Map<String, Object> resultMap = new HashMap<>();

        //这里查询两次数据库
        List<ProjectTeam> teams = projectTeamMapper.selectAllByUser(userId);
        List<Project> projects = projectMapper.selectAll();

        //这里使用服务器资源进行循环代替多次查询数据库，实际上哪个性能高并没有进行测试，目前的情况是服务器性能高于数据库服务器性能
        List<Project> results = new ArrayList<>();

        for (int i = 0; i < projects.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (projects.get(i).getId().equals(teams.get(j).getProjectId())) {
                    //相等表示该用户参与该项目
                    results.add(projects.get(i));
                    //此次循环可以打断
                    break;
                }
            }
        }

        List<Project> listInMap = new ArrayList<>();

        for (int i = (page - 1) * pageSize; i < results.size(); i++) {
            listInMap.add(results.get(i));
            if (listInMap.size() >= pageSize) {
                break;
            }
        }

        resultMap.put("pageNum", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("pages", (results.size() + pageSize - 1) / pageSize);
        resultMap.put("total", results.size());
        resultMap.put("list",listInMap);

        return resultMap;
    }

    @Override
    public Object getAllProjectsByState(int page, int pageSize, Short state) {

        Map<String, Object> resultMap = new HashMap<>();

        List<Project> results = new ArrayList<>();

        List<Project> projects = projectMapper.selectAll();

        for (int i = 0; i < projects.size(); i++) {

            Project project = projects.get(i);

            if (project.getProjectState().equals(state)) {
                results.add(project);
            }
        }

        List<Project> listInMap = new ArrayList<>();

        for (int i = (page - 1) * pageSize; i < results.size(); i++) {
            listInMap.add(results.get(i));
            if (listInMap.size() >= pageSize) {
                break;
            }
        }

        resultMap.put("pageNum", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("pages", (results.size() + pageSize - 1) / pageSize);
        resultMap.put("total", results.size());
        resultMap.put("list",listInMap);

        return resultMap;
    }

    @Override
    public Object getAllProjectsByQuery(String query, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByQuery(query);
        return new PageInfo<>(projects);
    }

    @Override
    public Object getAllProjectsByQueryByUser(String query, Integer userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByQuery(query);
        List<ProjectTeam> teams = projectTeamMapper.selectAllByUser(userId);
        List<Project> userProjects = new ArrayList<>();  //用户项目
        //这里依旧是用服务器性能换取数据库稳定，这里在项目不多的情况下选择了使用模糊查询，后期开发建议使用全文搜索引擎等方法
        for (int i = 0; i < projects.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (projects.get(i).getId().equals(teams.get(j).getProjectId())) {
                    //相等表示该用户参与该项目
                    userProjects.add(projects.get(i));
                    //此次循环可以打断
                    break;
                }
            }
        }
        return new PageInfo<>(userProjects);
    }

    @Override
    public Object getAllProjectsByStateByUser(int page, int pageSize, Short state, Integer userId) {

        Map<String, Object> resultMap = new HashMap<>();

        List<Project> results = new ArrayList<>();

        //得到根据用户查询到的所有项目
        Object projectsByUser = getAllProjectsByUser(page, pageSize, userId);

        List<Project> projects = (List<Project>) ((Map<String, Object>)projectsByUser).get("list");

        for (int i = 0; i < projects.size(); i++) {

            Project project = projects.get(i);

            if (project.getProjectState().equals(state)) {
                results.add(project);
            }
        }

        List<Project> listInMap = new ArrayList<>();

        for (int i = (page - 1) * pageSize; i < results.size(); i++) {
            listInMap.add(results.get(i));
            if (listInMap.size() >= pageSize) {
                break;
            }
        }

        resultMap.put("pageNum", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put("pages", (results.size() + pageSize - 1) / pageSize);
        resultMap.put("total", results.size());
        resultMap.put("list",listInMap);

        return resultMap;
    }

    @Override
    public Object getAllProjectsByDate(Date start, Date end, Integer page, Integer pageSize) {
        //这里借助project这个model传递参数
        Project project = new Project();
        project.setStartTime(start);
        project.setFinishTime(end);
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByDate(project);
        return new PageInfo<>(projects);
    }

    @Override
    public Object getAllProjectsByDate(Date start, Date end, Integer page, Integer pageSize, Integer userId) {
        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByUserByDate(start, end, userId);
        return new PageInfo<>(projects);
    }

    @Override
    @Transactional
    public boolean setProjectStart(int projectId) {
        Project project = new Project();
        project.setId(projectId);

        short projectState = 1;  //1表示已启动
        project.setProjectState(projectState);
        project.setStartTime(new Date());

        int row = projectMapper.updateByPrimaryKeySelective(project);

        // 在启动项目后，启动所有的项目阶段
        List<Moment> moments = momentMapper.selectAllByProjectId(projectId);
        for (int i = 0; i < moments.size(); i++) {
            // 这里调用下面的方法，启动项目阶段
            momentStart(projectId, moments.get(i).getId());
        }

        if (row < 1) {
            return false;
        }

        return true;
    }

    @Override
    public boolean setProjectFinalCheck(int projectId) {
        Project project = new Project();
        project.setId(projectId);

        short projectState = 2;  //2表示终验
        project.setProjectState(projectState);

        int row = projectMapper.updateByPrimaryKeySelective(project);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean setProjectFinished(int projectId) {
        Project project = new Project();
        project.setId(projectId);

        short projectState = 3;  //3表示已完成
        project.setProjectState(projectState);

        int row = projectMapper.updateByPrimaryKeySelective(project);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public List<Moment> getAllMoments(int projectId) {
        return momentMapper.selectAllByProjectId(projectId);
    }

    @Override
    public boolean momentStart(int projectId, int momentId) {
        Moment moment = momentMapper.selectByPrimaryKey(momentId);

        if (moment.getProjectId() != projectId) {
            return false;
        }

        short state = 1;  //1表示已开始，即正在进行
        moment.setState(state);
        moment.setStartTime(new Date());
        int row = momentMapper.updateByPrimaryKeySelective(moment);

        if (row < 1) {
            return false;
        }

        return true;
    }

    @Override
    public boolean momentFinish(int projectId, int momentId) {
        Moment moment = momentMapper.selectByPrimaryKey(momentId);

        if (moment.getProjectId() != projectId) {
            return false;
        }

        short state = 2;  //2表示已完成
        moment.setState(state);
        moment.setFinishTime(new Date());  //完成时间
        int row = momentMapper.updateByPrimaryKeySelective(moment);

        if (row < 1) {
            return false;
        }

        return true;
    }

    @Override
    public List<Moment> getProjectMomentRunning(int projectId) {
        short state = 1;
        Moment moment = new Moment();
        moment.setProjectId(projectId);
        moment.setState(state);

        return momentMapper.selectByProjectIdAndState(moment);
    }

    @Override
    public Moment getMoment(int momentId) {
        return momentMapper.selectByPrimaryKey(momentId);
    }

    @Override
    public List<ProjectType> getAllProjectTypes() {
        return projectTypeMapper.selectAll();
    }

    @Override
    public ProjectProcess getProjectProcessByProject(int projectId) {
        return projectProcessMapper.selectByProject(projectId);
    }
   /* @Override
    public ProjectProcess getProjectProcessByProjectState(int projectId,int projectState) {
        return projectProcessMapper.selectByProjectState(projectId,projectState);
    }*/
}
