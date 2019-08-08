package com.supervision.project.mapper;

import com.supervision.project.model.RoleCost;

import java.util.List;

public interface RoleCostMapper {
    int deleteByPrimaryKey(Integer roleId);

    int insert(RoleCost record);

    int insertSelective(RoleCost record);

    RoleCost selectByPrimaryKey(Integer roleId);

    int updateByPrimaryKeySelective(RoleCost record);

    int updateByPrimaryKey(RoleCost record);

    List<RoleCost> selectAll();
}