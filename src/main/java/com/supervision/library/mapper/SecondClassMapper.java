package com.supervision.library.mapper;

import com.supervision.library.model.SecondClass;

import java.util.List;

public interface SecondClassMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SecondClass record);

    int insertSelective(SecondClass record);

    SecondClass selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(SecondClass record);

    int updateByPrimaryKey(SecondClass record);

    List<SecondClass> selectAll();

    List<SecondClass> selectAllByParent(Integer parentId);
}