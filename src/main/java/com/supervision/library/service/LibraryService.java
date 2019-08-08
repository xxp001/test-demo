package com.supervision.library.service;

import com.github.pagehelper.PageInfo;
import com.supervision.library.model.Book;
import com.supervision.library.model.FirstClass;
import com.supervision.library.model.SecondClass;
import com.supervision.library.model.ThirdClass;

import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:service
 * @Author:TjSanshao
 * @Create:2019-05-03 10:55
 *
 **/
public interface LibraryService {

    List<Book> listBook();
    PageInfo<Book> listBookByPage(Integer page, Integer pageSize);
    PageInfo<Book> listBooksByPageByClass(Integer page, Integer pageSize, Integer parentClass);
    PageInfo<Book> listBooksByQueryByPage(String keywords, Integer page, Integer pageSize);

    List<FirstClass> listFirstClass();

    List<SecondClass> listSecondClass();
    List<SecondClass> listSecondClassByParentClass(Integer parentClass);

    List<ThirdClass> listThirdClass();
    List<ThirdClass> listThirdClassByParentClass(Integer parentClass);

    Book getBookById(Integer id);

    FirstClass getFirstClassById(Integer id);

    SecondClass getSecondClassById(Integer id);

    ThirdClass getThirdClassById(Integer id);

    boolean saveBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(Integer id);

    boolean saveFirstClass(FirstClass firstClass);
    boolean updateFirstClass(FirstClass firstClass);

    boolean saveSecondClass(SecondClass secondClass);
    boolean updateSecondClass(SecondClass secondClass);

    boolean saveThirdClass(ThirdClass thirdClass);
    boolean updateThirdClass(ThirdClass thirdClass);
}
