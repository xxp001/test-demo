package com.supervision.document.mapper;

import com.supervision.document.model.Document;

import java.util.List;

public interface DocumentMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Document record);

    int insertSelective(Document record);

    Document selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Document record);

    int updateByPrimaryKey(Document record);

    List<Document> selectAllByProject(Integer projectId);

    List<Document> selectAllByMoment(Integer momentId);

    List<Document> selectAllByMomentIsTemplate(Integer momentId);
}