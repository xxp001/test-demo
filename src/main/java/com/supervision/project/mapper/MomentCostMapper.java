package com.supervision.project.mapper;

import com.supervision.project.model.MomentCost;

import java.util.List;

public interface MomentCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MomentCost record);

    int insertSelective(MomentCost record);

    MomentCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MomentCost record);

    int updateByPrimaryKey(MomentCost record);

    MomentCost selectByProjectIdAndMomentId(MomentCost record);

    List<MomentCost> selectByProjectId(int projectId);
}