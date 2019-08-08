package com.supervision.library.mapper;

import com.supervision.library.model.Book;

import java.util.List;

public interface BookMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Book record);

    int insertSelective(Book record);

    Book selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Book record);

    int updateByPrimaryKey(Book record);

    List<Book> selectAll();

    List<Book> selectAllByClass(Integer classId);

    List<Book> selectAllByKeywords(String keywords);
}