package com.supervision.census.service.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.census.model.CensusProjectCost;
import com.supervision.census.service.CensusService;
import com.supervision.project.mapper.*;
import com.supervision.project.model.ExtraCost;
import com.supervision.project.model.HumanCostRecord;
import com.supervision.project.model.Project;
import com.supervision.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:census service impl
 * @Author:TjSanshao
 * @Create:2019-04-13 19:57
 *
 **/
@Service
public class CensusServiceImpl implements CensusService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectTeamMapper projectTeamMapper;

    @Autowired
    private ExtraCostMapper extraCostMapper;

    @Autowired
    private HumanCostRecordMapper humanCostRecordMapper;

    @Autowired
    private RoleCostMapper roleCostMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Object listCensusProjectCosts(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        short state = 1;
        List<Project> projects = projectMapper.selectAllByStateAfter(state);
        // 分页信息
        PageInfo<Project> result = new PageInfo<>(projects);

        // 要返回的list
        List<CensusProjectCost> list = new LinkedList<>();

        // 遍历项目
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            projectTeamMapper.selectAllByProject(project.getId());

            CensusProjectCost censusProjectCost = new CensusProjectCost();
            censusProjectCost.setProject(project);
            censusProjectCost.setHumanCost(new BigDecimal(0));  // 人力
            censusProjectCost.setStay(new BigDecimal(0));  // 差旅
            censusProjectCost.setMeals(new BigDecimal(0));  // 伙食
            censusProjectCost.setTraffic(new BigDecimal(0));  // 交通
            censusProjectCost.setOthers(new BigDecimal(0));  // 其他

            List<ExtraCost> extraCosts = extraCostMapper.selectByProjectId(project.getId());
            List<HumanCostRecord> humanCostRecords = humanCostRecordMapper.selectByProjectId(project.getId());

            for (HumanCostRecord humanCostRecord : humanCostRecords) {
                BigDecimal roleCostHalfDay = roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay();
                BigDecimal humanCostByUser = new BigDecimal(humanCostRecord.getWorkTime().intValue()).multiply(roleCostHalfDay);
                censusProjectCost.setHumanCost(censusProjectCost.getHumanCost().add(humanCostByUser));
            }

            for (ExtraCost extraCost : extraCosts) {
                String costDesc = extraCost.getCostDesc();
                BigDecimal amount = extraCost.getAmount();
                if (costDesc.equals("差旅")) {
                    censusProjectCost.setStay(censusProjectCost.getStay().add(amount));
                } else if (costDesc.equals("伙食")) {
                    censusProjectCost.setMeals(censusProjectCost.getMeals().add(amount));
                } else if (costDesc.equals("交通")) {
                    censusProjectCost.setTraffic(censusProjectCost.getTraffic().add(amount));
                } else {
                    // 其他
                    censusProjectCost.setOthers(censusProjectCost.getOthers().add(amount));
                }
            }

            list.add(censusProjectCost);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", list);

        return resultMap;
    }

    @Override
    public Object listCensusProjectCostsByDate(Date start, Date end, int page, int pageSize) {

        Project projectQuery = new Project();
        projectQuery.setStartTime(start);
        projectQuery.setFinishTime(end);
        short state = 1;
        projectQuery.setProjectState(state);

        PageHelper.startPage(page, pageSize);
        List<Project> projects = projectMapper.selectAllByDateAndStateAfter(projectQuery);
        // 分页信息
        PageInfo<Project> result = new PageInfo<>(projects);

        // 要返回的list
        List<CensusProjectCost> list = new LinkedList<>();

        // 遍历项目
        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);

            // 这里懒得改，投机取巧，将buildName换掉，让前端显示正常
            project.setBuildName(userMapper.selectByPrimaryKey(project.getProjectDirector()).getUserRealname());

            projectTeamMapper.selectAllByProject(project.getId());

            CensusProjectCost censusProjectCost = new CensusProjectCost();
            censusProjectCost.setProject(project);
            censusProjectCost.setHumanCost(new BigDecimal(0));  // 人力
            censusProjectCost.setStay(new BigDecimal(0));  // 差旅
            censusProjectCost.setMeals(new BigDecimal(0));  // 伙食
            censusProjectCost.setTraffic(new BigDecimal(0));  // 交通
            censusProjectCost.setOthers(new BigDecimal(0));  // 其他

            List<ExtraCost> extraCosts = extraCostMapper.selectByProjectId(project.getId());
            List<HumanCostRecord> humanCostRecords = humanCostRecordMapper.selectByProjectId(project.getId());

            for (HumanCostRecord humanCostRecord : humanCostRecords) {
                BigDecimal roleCostHalfDay = roleCostMapper.selectByPrimaryKey(userMapper.selectByPrimaryKey(humanCostRecord.getUploadFrom()).getRoleId()).getRoleCostHalfDay();
                BigDecimal humanCostByUser = new BigDecimal(humanCostRecord.getWorkTime().intValue()).multiply(roleCostHalfDay);
                censusProjectCost.setHumanCost(censusProjectCost.getHumanCost().add(humanCostByUser));
            }

            for (ExtraCost extraCost : extraCosts) {
                String costDesc = extraCost.getCostDesc();
                BigDecimal amount = extraCost.getAmount();
                if (costDesc.equals("差旅")) {
                    censusProjectCost.setStay(censusProjectCost.getStay().add(amount));
                } else if (costDesc.equals("伙食")) {
                    censusProjectCost.setMeals(censusProjectCost.getMeals().add(amount));
                } else if (costDesc.equals("交通")) {
                    censusProjectCost.setTraffic(censusProjectCost.getTraffic().add(amount));
                } else {
                    // 其他
                    censusProjectCost.setOthers(censusProjectCost.getOthers().add(amount));
                }
            }

            list.add(censusProjectCost);
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("pageNum", result.getPageNum());
        resultMap.put("pageSize", result.getPageSize());
        resultMap.put("pages", result.getPages());
        resultMap.put("list", list);

        return resultMap;
    }
}
