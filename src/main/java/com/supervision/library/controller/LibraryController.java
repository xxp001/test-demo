package com.supervision.library.controller;

import com.github.pagehelper.PageInfo;
import com.supervision.common.config.ServerConfig;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import com.supervision.library.ViewModel.ViewBook;
import com.supervision.library.ViewModel.ViewFirstClass;
import com.supervision.library.ViewModel.ViewSecondClass;
import com.supervision.library.ViewModel.ViewThirdClass;
import com.supervision.library.model.Book;
import com.supervision.library.model.FirstClass;
import com.supervision.library.model.SecondClass;
import com.supervision.library.model.ThirdClass;
import com.supervision.library.service.LibraryService;
import com.supervision.user.model.User;
import com.supervision.user.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:controller
 * @Author:TjSanshao
 * @Create:2019-05-03 10:56
 *
 **/
@Controller
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private UserService userService;

    // 获取最近4个更改的book
    @ResponseBody
    @RequestMapping(value = "/library/recent", method = {RequestMethod.GET, RequestMethod.POST})
    public String recentBooks() {

        PageInfo<Book> bookPageInfo = libraryService.listBookByPage(1, 4);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewBooksBuild(bookPageInfo.getList()));
    }

    // 查询
    @ResponseBody
    @RequestMapping(value = "/library/query", method = {RequestMethod.GET, RequestMethod.POST})
    public String queryBooks(String keywords, Integer page, Integer pageSize) {

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        PageInfo<Book> bookPageInfo = libraryService.listBooksByQueryByPage(keywords, page, pageSize);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.resultBuild(bookPageInfo, bookPageInfo.getList()));
    }

    // 获取所有book
    @ResponseBody
    @RequestMapping(value = "/library/all", method = {RequestMethod.GET, RequestMethod.POST})
    public String allBooks(Integer page, Integer pageSize) {

        // 默认分页
        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        PageInfo<Book> bookPageInfo = libraryService.listBookByPage(page, pageSize);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.resultBuild(bookPageInfo, bookPageInfo.getList()));
    }

    // 获取所有firstClass
    @ResponseBody
    @RequestMapping(value = "/library/fist", method = {RequestMethod.GET, RequestMethod.POST})
    public String allFirstClasses() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, libraryService.listFirstClass());
    }

    // 获取所有secondClass
    @ResponseBody
    @RequestMapping(value = "/library/second", method = {RequestMethod.GET, RequestMethod.POST})
    public String allSecondClasses() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewSecondClassesBuild(libraryService.listSecondClass()));
    }

    // 获取所有secondClass
    @ResponseBody
    @RequestMapping(value = "/library/third", method = {RequestMethod.GET, RequestMethod.POST})
    public String allThirdClasses() {
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewThirdClassesBuild(libraryService.listThirdClass()));
    }

    // 获取所有Class
    @ResponseBody
    @RequestMapping(value = "/library/class", method = {RequestMethod.GET, RequestMethod.POST})
    public String allClasses() {
        // 通过firstClass获取class列表，这个可以用于返回分类数据
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewFirstClassesBuild(libraryService.listFirstClass()));
    }

    // 根据分类获取book
    @ResponseBody
    @RequestMapping(value = "/library/books_class", method = {RequestMethod.GET, RequestMethod.POST})
    public String allBooksByClass(Integer classId, Integer page, Integer pageSize) {

        // 默认分页
        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        PageInfo<Book> bookPageInfo = libraryService.listBooksByPageByClass(page, pageSize, classId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.resultBuild(bookPageInfo, bookPageInfo.getList()));
    }

    // 创建book
    @ResponseBody
    @RequestMapping(value = "/library/book_build", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String addBook(Book book, MultipartFile file) {
        if (file == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.UPLOAD_FILE_CANNOT_BE_NULL, null);
        }
        if (book.getBookName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_NAME_CANNOT_BE_NULL, null);
        }
        if (book.getThirdClass() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_CLASS_CANNOT_BE_NULL, null);
        }

        // 设置分类，只通过三级分类设置，上传无效
        book.setSecondClass(libraryService.getThirdClassById(book.getThirdClass()).getParentClass());
        book.setFirstClass(libraryService.getSecondClassById(book.getSecondClass()).getParentClass());

        book.setEditTime(new Date());

        // 获取登录用户
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        book.setUploadFrom(userInLogin.getId());

        String originalFilename = file.getOriginalFilename();
        String firstPath = libraryService.getFirstClassById(book.getFirstClass()).getClassName();
        String secondPath = libraryService.getSecondClassById(book.getSecondClass()).getClassName();
        String thirdPath = libraryService.getThirdClassById(book.getThirdClass()).getClassName();
        String location = serverConfig.getFileLocationPath() + File.separator + firstPath + File.separator + secondPath + File.separator + thirdPath + File.separator + book.getBookName() + originalFilename.substring(originalFilename.lastIndexOf("."));
        File dest = new File(location);
        //判断路径是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            book.setBookLocation(location);
            libraryService.saveBook(book);
            book.setBookLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, book);
        } catch (IOException ex) {
            //TODO 记录
            ex.printStackTrace();
            book.setBookLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, book);
        }
    }

    // 更新book
    @ResponseBody
    @RequestMapping(value = "/library/book_update", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String updateBook(Book book, MultipartFile file) {
        if (book.getId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        if (book.getBookName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_NAME_CANNOT_BE_NULL, null);
        }
        if (book.getThirdClass() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_CLASS_CANNOT_BE_NULL, null);
        }

        Book bookById = libraryService.getBookById(book.getId());
        if (bookById == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        // 设置分类，只通过三级分类设置，上传无效
        book.setSecondClass(libraryService.getThirdClassById(book.getThirdClass()).getParentClass());
        book.setFirstClass(libraryService.getSecondClassById(book.getSecondClass()).getParentClass());
        book.setEditTime(new Date());

        // 获取登录用户
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        book.setUploadFrom(userInLogin.getId());

        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            String firstPath = libraryService.getFirstClassById(book.getFirstClass()).getClassName();
            String secondPath = libraryService.getSecondClassById(book.getSecondClass()).getClassName();
            String thirdPath = libraryService.getThirdClassById(book.getThirdClass()).getClassName();
            String location = serverConfig.getFileLocationPath() + File.separator + firstPath + File.separator + secondPath + File.separator + thirdPath + File.separator + book.getBookName() + originalFilename.substring(originalFilename.lastIndexOf("."));
            File dest = new File(location);
            //判断路径是否存在
            File existsFile = new File(bookById.getBookLocation());
            if (existsFile.exists()) {
                // 如果文件已经存在，删除
                existsFile.delete();
            }
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                file.transferTo(dest);
                book.setBookLocation(location);
                libraryService.updateBook(book);
                book.setBookLocation("");
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, book);
            } catch (IOException ex) {
                //TODO 记录
                ex.printStackTrace();
                book.setBookLocation("");
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, book);
            }
        } else {
            libraryService.updateBook(book);
            book.setBookLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, book);
        }
    }

    // 删除book
    @ResponseBody
    @RequestMapping(value = "/library/book_delete", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String deleteBook(Integer bookId) {
        if (bookId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        Book book = libraryService.getBookById(bookId);

        // 如果该id不存在
        if (book == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        String bookLocation = book.getBookLocation();
        if (!StringUtils.isEmpty(bookLocation)) {
            File file = new File(bookLocation);
            if (file.exists()) {
                // 删除
                file.delete();
            }
        }

        // 直接删除记录
        boolean result = libraryService.deleteBook(bookId);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 查询book
    @ResponseBody
    @RequestMapping(value = "/library/book", method = {RequestMethod.GET, RequestMethod.POST})
    public String getBook(Integer bookId) {
        if (bookId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        Book book = libraryService.getBookById(bookId);

        // 如果该id不存在
        if (book == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS,  StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewBookBuild(libraryService.getBookById(bookId)));
    }

    // 下载book
    @RequestMapping(value = "/library/download", method = {RequestMethod.GET})
    public void downloadBook(Integer bookId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (bookId == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("请选择文档！");
            return;
        }
        Book book = libraryService.getBookById(bookId);

        // 如果该id不存在
        if (book == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("请选择文档！");
            return;
        }

        File file = new File(book.getBookLocation());

        if (file.exists()) {
            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            byte[] buffer = new byte[1024];
            FileInputStream fileInputStream = null;
            BufferedInputStream bufferedInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                OutputStream outputStream = response.getOutputStream();
                int i = bufferedInputStream.read(buffer);
                while(i != -1) {
                    outputStream.write(buffer, 0, i);
                    i = bufferedInputStream.read(buffer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("文件不存在！（未上传或系统错误）");
            return;
        }
    }

    // 创建一级分类
    @ResponseBody
    @RequestMapping(value = "/library/first_build", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String addFirstClass(FirstClass firstClass) {
        if (firstClass.getClassName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_NAME_CANNOT_BE_NULL, null);
        }

        firstClass.setKeywords(firstClass.getKeywords() + "," + firstClass.getClassName() + "," + firstClass.getClassDesc());

        boolean result = libraryService.saveFirstClass(firstClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 更新一级分类
    @ResponseBody
    @RequestMapping(value = "/library/first_update", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String updateFirstClass(FirstClass firstClass) {
        if (firstClass.getId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        FirstClass firstClassInDb = libraryService.getFirstClassById(firstClass.getId());
        if (firstClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        firstClass.setKeywords(firstClass.getKeywords() + "," + firstClass.getClassName() + "," + firstClass.getClassDesc());

        boolean result = libraryService.updateFirstClass(firstClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 查询一级分类
    @ResponseBody
    @RequestMapping(value = "/library/firstClass", method = {RequestMethod.GET, RequestMethod.POST})
    public String getFirstClass(Integer firstClassId) {
        if (firstClassId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        FirstClass firstClassInDb = libraryService.getFirstClassById(firstClassId);
        if (firstClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewFirstClassBuild(firstClassInDb));
    }

    // 创建二级分类
    @ResponseBody
    @RequestMapping(value = "/library/second_build", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String addSecondClass(SecondClass secondClass) {
        if (secondClass.getClassName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_NAME_CANNOT_BE_NULL, null);
        }
        if (secondClass.getParentClass() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "必须指定一级分类！", null);
        }

        FirstClass firstClassById = libraryService.getFirstClassById(secondClass.getParentClass());

        secondClass.setKeywords(secondClass.getKeywords() + "," + secondClass.getClassName() + "," + secondClass.getClassDesc() + "," + firstClassById.getKeywords());

        boolean result = libraryService.saveSecondClass(secondClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 更新二级分类
    @ResponseBody
    @RequestMapping(value = "/library/second_update", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String updateSecondClass(SecondClass secondClass) {
        if (secondClass.getId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        SecondClass secondClassInDb = libraryService.getSecondClassById(secondClass.getId());
        if (secondClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        FirstClass firstClassById = libraryService.getFirstClassById(secondClass.getParentClass());

        secondClass.setKeywords(secondClass.getKeywords() + "," + secondClass.getClassName() + "," + secondClass.getClassDesc() + "," + firstClassById.getKeywords());

        boolean result = libraryService.updateSecondClass(secondClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 查询二级分类
    @ResponseBody
    @RequestMapping(value = "/library/secondClass", method = {RequestMethod.GET, RequestMethod.POST})
    public String getSecondClass(Integer secondClassId) {
        if (secondClassId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        SecondClass secondClassInDb = libraryService.getSecondClassById(secondClassId);
        if (secondClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewSecondClassBuild(secondClassInDb));
    }

    // 创建三级分类
    @ResponseBody
    @RequestMapping(value = "/library/third_build", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String addThirdClass(ThirdClass thirdClass) {
        if (thirdClass.getClassName() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_NAME_CANNOT_BE_NULL, null);
        }
        if (thirdClass.getParentClass() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "必须指定二级分类！", null);
        }

        SecondClass secondClassById = libraryService.getSecondClassById(thirdClass.getParentClass());

        thirdClass.setKeywords(thirdClass.getKeywords() + "," + thirdClass.getClassName() + "," + thirdClass.getClassDesc() + "," + secondClassById.getKeywords());

        boolean result = libraryService.saveThirdClass(thirdClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 更新三级分类
    @ResponseBody
    @RequestMapping(value = "/library/third_update", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.LIBRARY_EDIT})
    public String updateThirdClass(ThirdClass thirdClass) {
        if (thirdClass.getId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        ThirdClass thirdClassInDb = libraryService.getThirdClassById(thirdClass.getId());
        if (thirdClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        SecondClass secondClassById = libraryService.getSecondClassById(thirdClass.getParentClass());

        thirdClass.setKeywords(thirdClass.getKeywords() + "," + thirdClass.getClassName() + "," + thirdClass.getClassDesc() + "," + secondClassById.getKeywords());

        boolean result = libraryService.updateThirdClass(thirdClass);

        if (result) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
        } else {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
        }
    }

    // 查询三级分类
    @ResponseBody
    @RequestMapping(value = "/library/thirdClass", method = {RequestMethod.GET, RequestMethod.POST})
    public String getThirdClass(Integer thirdClassId) {
        if (thirdClassId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }
        ThirdClass thirdClassInDb = libraryService.getThirdClassById(thirdClassId);
        if (thirdClassInDb == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.BOOK_ID_CANNOT_BE_NULL, null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, this.viewThirdClassBuild(thirdClassInDb));
    }

    // 构建视图viewBook
    private ViewBook viewBookBuild(Book book) {
        ViewBook viewBook = new ViewBook();
        viewBook.setId(book.getId());
        viewBook.setBookName(book.getBookName());
        viewBook.setBookDesc(book.getBookDesc());
        viewBook.setBookKeywords(book.getBookKeywords());
        viewBook.setBookLocation("");
        viewBook.setFirstClassId(book.getFirstClass());
        viewBook.setFirstClass(libraryService.getFirstClassById(book.getFirstClass()));
        viewBook.setSecondClassId(book.getSecondClass());
        viewBook.setSecondClass(libraryService.getSecondClassById(book.getSecondClass()));
        viewBook.setThirdClassId(book.getThirdClass());
        viewBook.setThirdClass(libraryService.getThirdClassById(book.getThirdClass()));
        viewBook.setEditTime(book.getEditTime());
        viewBook.setUploadFrom(userService.getUser(book.getUploadFrom()));
        viewBook.setUploadName(userService.getUser(book.getUploadFrom()).getUserRealname());
        return viewBook;
    }

    // 构建视图viewBookList
    private List<ViewBook> viewBooksBuild(List<Book> books) {
        List<ViewBook> list = new LinkedList<>();
        for (int i = 0; i < books.size(); i++) {
            list.add(this.viewBookBuild(books.get(i)));
        }
        return list;
    }

    // 构建视图viewFirstClass
    private ViewFirstClass viewFirstClassBuild(FirstClass firstClass) {
        ViewFirstClass viewFirstClass = new ViewFirstClass();
        viewFirstClass.setId(firstClass.getId());
        viewFirstClass.setClassName(firstClass.getClassName());
        viewFirstClass.setClassDesc(firstClass.getClassDesc());
        viewFirstClass.setKeywords(firstClass.getKeywords());

        List<SecondClass> secondClasses = libraryService.listSecondClassByParentClass(firstClass.getId());
        List<ViewSecondClass> viewSecondClasses = this.viewSecondClassesBuild(secondClasses);

        viewFirstClass.setChildClasses(viewSecondClasses);

        return viewFirstClass;

    }

    // 构建视图viewFirstClassList
    private List<ViewFirstClass> viewFirstClassesBuild(List<FirstClass> firstClasses) {
        List<ViewFirstClass> list = new LinkedList<>();
        for (int i = 0; i < firstClasses.size(); i++) {
            list.add(this.viewFirstClassBuild(firstClasses.get(i)));
        }
        return list;
    }

    // 构建视图viewSecondClass
    private ViewSecondClass viewSecondClassBuild(SecondClass secondClass) {
        ViewSecondClass viewSecondClass = new ViewSecondClass();
        viewSecondClass.setId(secondClass.getId());
        viewSecondClass.setClassName(secondClass.getClassName());
        viewSecondClass.setClassDesc(secondClass.getClassDesc());
        viewSecondClass.setKeywords(secondClass.getKeywords());
        viewSecondClass.setParentClassId(secondClass.getParentClass());
        viewSecondClass.setParentClass(libraryService.getFirstClassById(secondClass.getParentClass()));

        List<ThirdClass> thirdClasses = libraryService.listThirdClassByParentClass(secondClass.getId());
        List<ViewThirdClass> viewThirdClasses = this.viewThirdClassesBuild(thirdClasses);

        viewSecondClass.setChildClasses(viewThirdClasses);

        return viewSecondClass;
    }

    // 构建视图viewSecondClassList
    private List<ViewSecondClass> viewSecondClassesBuild(List<SecondClass> secondClasses) {
        List<ViewSecondClass> list = new LinkedList<>();
        for (int i = 0; i < secondClasses.size(); i++) {
            list.add(this.viewSecondClassBuild(secondClasses.get(i)));
        }
        return list;
    }

    // 构建视图viewThirdClass
    private ViewThirdClass viewThirdClassBuild(ThirdClass thirdClass) {
        ViewThirdClass viewThirdClass = new ViewThirdClass();
        viewThirdClass.setId(thirdClass.getId());
        viewThirdClass.setClassName(thirdClass.getClassName());
        viewThirdClass.setClassDesc(thirdClass.getClassDesc());
        viewThirdClass.setKeywords(thirdClass.getKeywords());
        viewThirdClass.setParentClassId(thirdClass.getParentClass());
        viewThirdClass.setParentClass(libraryService.getSecondClassById(thirdClass.getParentClass()));
        return viewThirdClass;
    }

    // 构建视图构建视图viewThirdClassList
    private List<ViewThirdClass> viewThirdClassesBuild(List<ThirdClass> thirdClasses) {
        List<ViewThirdClass> list = new LinkedList<>();
        for (int i = 0; i < thirdClasses.size(); i++) {
            list.add(this.viewThirdClassBuild(thirdClasses.get(i)));
        }
        return list;
    }

    // 构建分页返回数据
    private Map<String, Object> resultBuild(PageInfo pageInfo, List list) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", pageInfo.getTotal());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        result.put("pages", pageInfo.getPages());
        result.put("list", viewBooksBuild(list));
        return result;
    }

}
