package com.supervision.project.mapper;

import com.supervision.project.model.Moment;

import java.util.List;

public interface MomentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Moment record);

    int insertSelective(Moment record);

    Moment selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Moment record);

    int updateByPrimaryKey(Moment record);

    List<Moment> selectAllByProjectId(int projectId);

    List<Moment> selectByProjectIdAndState(Moment record);
}