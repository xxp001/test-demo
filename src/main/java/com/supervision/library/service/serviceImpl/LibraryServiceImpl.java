package com.supervision.library.service.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.library.mapper.BookMapper;
import com.supervision.library.mapper.FirstClassMapper;
import com.supervision.library.mapper.SecondClassMapper;
import com.supervision.library.mapper.ThirdClassMapper;
import com.supervision.library.model.Book;
import com.supervision.library.model.FirstClass;
import com.supervision.library.model.SecondClass;
import com.supervision.library.model.ThirdClass;
import com.supervision.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:service impl
 * @Author:TjSanshao
 * @Create:2019-05-03 10:55
 *
 **/
@Service
public class LibraryServiceImpl implements LibraryService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private FirstClassMapper firstClassMapper;

    @Autowired
    private SecondClassMapper secondClassMapper;

    @Autowired
    private ThirdClassMapper thirdClassMapper;

    @Override
    public List<Book> listBook() {
        return bookMapper.selectAll();
    }

    @Override
    public PageInfo<Book> listBookByPage(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Book> books = bookMapper.selectAll();
        return new PageInfo<>(books);
    }

    @Override
    public PageInfo<Book> listBooksByPageByClass(Integer page, Integer pageSize, Integer parentClass) {
        PageHelper.startPage(page, pageSize);
        List<Book> books = bookMapper.selectAllByClass(parentClass);
        return new PageInfo<>(books);
    }

    @Override
    public PageInfo<Book> listBooksByQueryByPage(String keywords, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Book> books = bookMapper.selectAllByKeywords(keywords);
        return new PageInfo<>(books);
    }

    @Override
    public List<FirstClass> listFirstClass() {
        return firstClassMapper.selectAll();
    }

    @Override
    public List<SecondClass> listSecondClass() {
        return secondClassMapper.selectAll();
    }

    @Override
    public List<SecondClass> listSecondClassByParentClass(Integer parentClass) {
        return secondClassMapper.selectAllByParent(parentClass);
    }

    @Override
    public List<ThirdClass> listThirdClass() {
        return thirdClassMapper.selectAll();
    }

    @Override
    public List<ThirdClass> listThirdClassByParentClass(Integer parentClass) {
        return thirdClassMapper.selectAllByParent(parentClass);
    }

    @Override
    public Book getBookById(Integer id) {
        return bookMapper.selectByPrimaryKey(id);
    }

    @Override
    public FirstClass getFirstClassById(Integer id) {
        return firstClassMapper.selectByPrimaryKey(id);
    }

    @Override
    public SecondClass getSecondClassById(Integer id) {
        return secondClassMapper.selectByPrimaryKey(id);
    }

    @Override
    public ThirdClass getThirdClassById(Integer id) {
        return thirdClassMapper.selectByPrimaryKey(id);
    }

    @Override
    public boolean saveBook(Book book) {

        int i = bookMapper.insertSelective(book);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean updateBook(Book book) {

        int i = bookMapper.updateByPrimaryKeySelective(book);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteBook(Integer id) {

        int i = bookMapper.deleteByPrimaryKey(id);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean saveFirstClass(FirstClass firstClass) {

        int i = firstClassMapper.insertSelective(firstClass);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean updateFirstClass(FirstClass firstClass) {

        int i = firstClassMapper.updateByPrimaryKeySelective(firstClass);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean saveSecondClass(SecondClass secondClass) {

        int i = secondClassMapper.insertSelective(secondClass);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean updateSecondClass(SecondClass secondClass) {

        int i = secondClassMapper.updateByPrimaryKeySelective(secondClass);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean saveThirdClass(ThirdClass thirdClass) {
        int i = thirdClassMapper.insertSelective(thirdClass);

        if (i > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean updateThirdClass(ThirdClass thirdClass) {
        int i = thirdClassMapper.updateByPrimaryKeySelective(thirdClass);

        if (i > 0) {
            return true;
        }

        return false;
    }
}
