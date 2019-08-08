package com.supervision.project.mapper;

import com.supervision.project.model.HumanCostRecord;

import java.util.List;

public interface HumanCostRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(HumanCostRecord record);

    int insertSelective(HumanCostRecord record);

    HumanCostRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(HumanCostRecord record);

    int updateByPrimaryKey(HumanCostRecord record);

    List<HumanCostRecord> selectByProjectId(int projectId);

    List<HumanCostRecord> selectByProjectAndUser(int projectId, int userId);

    List<HumanCostRecord> selectByMomentAndUser(int momentId, int userId);

    List<HumanCostRecord> selectByMoment(int momentId);

    HumanCostRecord selectHumanCostRecordByUploaderAndProjectAndUploadTime(HumanCostRecord record);

    HumanCostRecord selectTheLastRecordByUpload(Integer uploadFrom, Integer projectId);
}