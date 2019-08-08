package com.supervision.library.mapper;

import com.supervision.library.model.ThirdClass;

import java.util.List;

public interface ThirdClassMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ThirdClass record);

    int insertSelective(ThirdClass record);

    ThirdClass selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ThirdClass record);

    int updateByPrimaryKey(ThirdClass record);

    List<ThirdClass> selectAll();

    List<ThirdClass> selectAllByParent(Integer parentId);
}