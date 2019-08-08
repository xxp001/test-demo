package com.supervision.document.mapper;

import com.supervision.document.model.DocType;

import java.util.List;

public interface DocTypeMapper {
    int deleteByPrimaryKey(Integer docTypeId);

    int insert(DocType record);

    int insertSelective(DocType record);

    DocType selectByPrimaryKey(Integer docTypeId);

    int updateByPrimaryKeySelective(DocType record);

    int updateByPrimaryKey(DocType record);

    List<DocType> selectAll();

    DocType selectByTemplateName(String templateName);
}