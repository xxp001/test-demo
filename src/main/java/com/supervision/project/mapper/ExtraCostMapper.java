package com.supervision.project.mapper;

import com.supervision.project.model.ExtraCost;

import java.util.Date;
import java.util.List;

public interface ExtraCostMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ExtraCost record);

    int insertSelective(ExtraCost record);

    ExtraCost selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ExtraCost record);

    int updateByPrimaryKey(ExtraCost record);

    List<ExtraCost> selectByProjectId(int projectId);

    List<ExtraCost> selectByProjectAndUser(int projectId, int userId);

    List<ExtraCost> selectByProjectAndUserByTime(int projectId, int userId, Date uploadTime);

    List<ExtraCost> selectByMomentAndUserByTime(int momentId, int userId, Date uploadTime);

    List<ExtraCost> selectByMoment(int momentId);
}