package com.supervision.document.controller;

import com.alibaba.fastjson.JSON;
import com.baidu.ueditor.ActionEnter;
import com.supervision.common.config.ServerConfig;
import com.supervision.common.util.JsonUtil;
import com.supervision.common.util.PermissionStrings;
import com.supervision.common.util.ResponseMessage;
import com.supervision.common.util.StaticProperties;
import com.supervision.document.ViewModel.ViewDocumentByType;
import com.supervision.document.model.DocType;
import com.supervision.document.model.Document;
import com.supervision.document.service.DocumentService;
import com.supervision.document.util.Word2HtmlUtils;
import com.supervision.document.util.ZipUtils;
import com.supervision.project.model.Moment;
import com.supervision.project.model.Project;
import com.supervision.project.service.ProjectService;
import com.supervision.project.service.ProjectTeamService;
import com.supervision.user.model.User;
import com.supervision.user.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/*
 * @Project:SupervisionSystem
 * @Description:document controller
 * @Author:TjSanshao
 * @Create:2019-02-17 02:02
 *
 **/
@CrossOrigin
@Controller
@RequiresAuthentication
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectTeamService projectTeamService;

    @Autowired
    private UserService userService;

    // 获取项目的文档列表
    @ResponseBody
    @RequestMapping(value = "/document/list", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_READ})
    public String allDocuments(Integer projectId, Integer page, Integer pageSize) {
        if (projectId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL, "projectId Can not be NULL!");
        }

        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 10;
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        // 这里查找的是isTemplate属性不为1的文档，即非模板文档
        Object allDocsByProject = documentService.getAllDocsByProject(page, pageSize, projectId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, allDocsByProject);
    }

    // 获取项目阶段的文档列表
    @ResponseBody
    @RequestMapping(value = "/document/moment_list", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_READ})
    public String allDocumentsInMoment(Integer momentId) {
        if (momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "momentId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectService.getMoment(momentId).getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        // 这里查找非模板文档
        List<Document> allDocsByProject = documentService.getAllDocsByMoment(momentId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, allDocsByProject);
    }

    // 获取项目阶段的文档列表，根据类型分类
    @ResponseBody
    @RequestMapping(value = "/document/moment_list_type", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_READ})
    public String allDocumentsInMomentByType(Integer momentId) {
        if (momentId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "momentId Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectService.getMoment(momentId).getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        // 执行到这里，证明登录的用户参与了项目或者拥有高级权限

        // 获取所有的文档模板
        List<Document> docsByMomentIsTemplate = documentService.listDocsByMomentIsTemplate(momentId);

        // 获取所有有文件的文档
        List<Document> allDocsByMoment = documentService.getAllDocsByMoment(momentId);

        List<ViewDocumentByType> viewDocumentByTypes = new LinkedList<>();

        // 遍历这个阶段所有的docType
        for (Document docIsTemplate : docsByMomentIsTemplate) {
            ViewDocumentByType viewDocumentByType = new ViewDocumentByType();
            viewDocumentByType.setId(docIsTemplate.getId());  // 设置Id，前端需要根据Id来进行上传
            List<Document> tempList = new LinkedList<>();
            for (int i = 0; i < allDocsByMoment.size(); i++) {
                // 遍历该阶段所有文档
                if (allDocsByMoment.get(i).getDocType().equals(docIsTemplate.getDocType())) {
                    // 如果docType相等
                    tempList.add(allDocsByMoment.get(i));
                }
            }

            viewDocumentByType.setProjectId(docIsTemplate.getProjectId());
            viewDocumentByType.setMomentId(docIsTemplate.getMomentId());
            viewDocumentByType.setDocName(docIsTemplate.getDocName());
            viewDocumentByType.setFileCode(docIsTemplate.getFileCode());
            viewDocumentByType.setDocType(docIsTemplate.getDocType());
            viewDocumentByType.setDocuments(tempList);
            viewDocumentByTypes.add(viewDocumentByType);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, viewDocumentByTypes);
    }

    // 查询文档类型
    @ResponseBody
    @RequestMapping(value = "/document/types_list", method = {RequestMethod.GET, RequestMethod.POST})
    public String allDocType(Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, documentService.getAllDocType());
        }
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, documentService.listDocTypesByPage(page, pageSize));
    }

    // 上传文档
    @ResponseBody
    @RequestMapping(value = "/document/upload", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_UPLOAD})
    public String uploadDoc(Integer id, MultipartFile file) {

        if (file == null || id == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL + ResponseMessage.UPLOAD_FILE_CANNOT_BE_NULL, "id Or file Can not be NULL!");
        }

        // 这里获取到的应该是文档模板
        Document docInQuery = documentService.getDoc(id);

        if (projectService.getProject(docInQuery.getProjectId()).getProjectState().intValue() != 1) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "项目非处于进行状态，无法提交文档！", null);
        }

        if (docInQuery.getIsTemplate() != 1) {
            // 该Id不是文档模板，需要重新选择文档模板
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, null);
        }

        // 获取登录用户
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        Integer projectId = docInQuery.getProjectId();
        Integer momentId = docInQuery.getMomentId();

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        short state = 1;  //1表示已上传，状态正常，0表示未上传，2表示已删除
        docInQuery.setDocState(state);
        docInQuery.setUploadTime(new Date());
        docInQuery.setUploadFrom(userInLogin.getId());

        String originalFilename = file.getOriginalFilename();
        String firstPath = projectService.getProject(projectId).getProjectName();
        String secondPath = projectService.getMoment(momentId).getMomentDesc();

        int count = docInQuery.getCount() + 1;
        String suffix = "";
        if (count < 10) {
            suffix = "-00" + count;
        } else if (count >= 10 && count <100) {
            suffix = "-0" + count;
        } else{
            suffix = "-" + count;
        }
        String wholeName = docInQuery.getDocName() + suffix;

        String location = serverConfig.getFileLocationPath() + File.separator + firstPath + File.separator + secondPath + File.separator + wholeName + originalFilename.substring(originalFilename.lastIndexOf("."));
        File dest = new File(location);

        //判断路径是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
            docInQuery.setDocLocation(location);
            boolean result = documentService.uploadDoc(docInQuery);
            docInQuery.setDocLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, docInQuery);
        } catch (IOException ex) {
            //TODO 记录
            ex.printStackTrace();
            docInQuery.setDocLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, docInQuery);
        }
    }

    // 创建文档，需求调整，我很烦，这个接口废了
    //@ResponseBody
    //@RequestMapping(value = "/document/build", method = {RequestMethod.GET, RequestMethod.POST})
    public String buildDoc(Document doc, MultipartFile file) {

        if (doc.getProjectId() == null || doc.getMomentId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "projectId Or momentId Can not be NULL!");
        }

        if (file == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.UPLOAD_FILE_CANNOT_BE_NULL, "file Can not be NULL!");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User user = (User)session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(doc.getProjectId(), user.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        short state = 1;  //1表示已上传，状态正常，0表示未上传，2表示已删除
        doc.setDocState(state);
        doc.setUploadTime(new Date());
        doc.setUploadFrom(user.getId());
        doc.setUploaderName(user.getUserRealname());
        doc.setWorkCode(user.getWorkCode());

        String originalFilename = file.getOriginalFilename();
        String firstPath = projectService.getProject(doc.getProjectId()).getProjectName();
        String secondPath = projectService.getMoment(doc.getMomentId()).getMomentDesc();
        String location = serverConfig.getFileLocationPath() + File.separator + firstPath + File.separator + secondPath + File.separator + originalFilename;
        File dest = new File(location);

        //判断路径是否存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
            doc.setDocLocation(location);
            boolean result = documentService.buildDoc(doc);
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, doc);
        } catch (IOException ex) {
            //TODO 记录
            ex.printStackTrace();
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, doc);
        }
    }

    // 创建文档，不包含文件，需要创建权限
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_CREATE})
    @ResponseBody
    @RequestMapping(value = "/document/create", method = {RequestMethod.GET, RequestMethod.POST})
    public String buildDocWithoutFile(Document document) {
        if (document.getProjectId() == null || document.getMomentId() == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PROJECT_ID_CANNOT_BE_NULL + ResponseMessage.PROJECT_MOMENT_ID_CANNOT_BE_NULL, "projectId Or momentId Can not be NULL!");
        }

        List<Document> docsByMomentIsTemplate = documentService.listDocsByMomentIsTemplate(document.getMomentId());

        for (int i = 0; i < docsByMomentIsTemplate.size(); i++) {
            if (docsByMomentIsTemplate.get(i).getDocType().equals(document.getDocType())) {
                // docType和已存在的同一阶段的文档相同
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "已存在该文档模板！请审查！", null);
            }
        }

        short state = 0;  //1表示已上传，状态正常，0表示未上传，2表示已删除
        document.setDocState(state);

        boolean result = documentService.createDoc(document);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, document);
    }

    // 下载文档
    @RequestMapping(value = "/document/download", method = {RequestMethod.GET})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_DOWNLOAD})
    public void documentDownload(Integer docId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (docId == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("请选择文档！");
            return;
        }

        Document doc = documentService.getDoc(docId);

        if (doc.getDocState().intValue() != 1) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("该文档未上传，请重新选择！<a href='/'>&gt;&gt;点击此处回到首页</a>");
            return;
        }

        Subject subject = SecurityUtils.getSubject();
        User userInLogin = (User) subject.getSession().getAttribute(StaticProperties.SESSION_USER);
        // 判断是否参与了项目
        if (!projectTeamService.isInProject(doc.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().print(ResponseMessage.PERMISSION_ERROR);
                return;
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        File file = new File(doc.getDocLocation());

        if (file.exists()) {
            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(doc.getWholeName() + file.getName().substring(file.getName().lastIndexOf(".")), "UTF-8"));
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

    // 删除文档
    @ResponseBody
    @RequestMapping(value = "/document/delete", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_DELETE})
    public String documentDelete(Integer docId) {
        if (docId == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, "docId Can not be NULL!");
        }

        Document docInQuery = documentService.getDoc(docId);

        if (docInQuery.getDocState().intValue() == 0) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, "Deleted!");
        }

      Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(docInQuery.getProjectId(), userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
            // 进入这一步，登录的用户没有高级权限，没有高级权限的用户上传文档做判断
            // 如果这个文档的状态正常（即有人上传了文档，又没有删除）
            Short docState = docInQuery.getDocState();
            if (docState.intValue() == 1) {
                // 判断上传过文档的人是否就是登录的用户
                if (docInQuery.getUploadFrom().intValue() != userInLogin.getId()) {
                    // 如果这个上传过文档的人不是登录的用户
                    return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "不可删除他人已上传的文档！", null);
                }

                //执行到这里，登录的用户已经上传过文档，那么判断是否已超过24小时
                if (isMoreThan24Hours(docInQuery.getUploadTime(), new Date())) {
                    //如果超过了24小时
                    return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "已超过24小时，不可删除！", null);
                }
            }
        }

        //拥有高级权限，或者是本人删除未超过24小时的文档

        boolean result = documentService.deleteDoc(docId);

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, "Deleted!");
    }

    @ResponseBody
    @RequestMapping(value = "/document/template", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_READ})
    public String templateName(String templateName) {
        DocType docType = documentService.getDocTypeByTemplateName(templateName);
        if (docType == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "没有该文档模板！", null);
        }
        docType.setDocTemplateLocation("");
        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, docType);
    }

    //文档模板下载
    @RequestMapping(value = "/document/template_download", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_TEMPLATE_DOWNLOAD})
    public void templateDownload(String templateName, HttpServletResponse response) throws Exception {
        if (templateName == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("请输入模板名称！");
            return;
        }

        DocType docType = documentService.getDocTypeByTemplateName(templateName);

        if (docType == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("该模板不存在");
            return;
        }

        if (docType.getDocTemplateLocation() == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print("该模板尚未上传");
            return;
        }

        File file = new File(docType.getDocTemplateLocation());

        if (file.exists()) {
            // 配置文件下载
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(docType.getDocTypeName() + docType.getDocTypeDesc() + file.getName().substring(file.getName().lastIndexOf(".")), "UTF-8"));
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
            response.getWriter().print("该模板尚未上传");
        }
    }

    //文档模板上传
    //@RequiresPermissions(value = {PermissionStrings.DOCUMENT_TEMPLATE_UPLOAD})
    @ResponseBody
    @RequestMapping(value = "/document/template_upload", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_TEMPLATE_UPLOAD})
    public String docTemplateUploadWithFile(String docTypeName, String docTypeDesc, MultipartFile docFile) {
        if (docTypeName == null || docFile == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.UPLOAD_FILE_CANNOT_BE_NULL, null);
        }

        DocType docTypeByTemplateName = documentService.getDocTypeByTemplateName(docTypeName);
        if (docTypeByTemplateName != null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "模板已存在！请审查！", null);
        }

        DocType docType = new DocType();
        docType.setDocTypeName(docTypeName);
        docType.setDocTypeDesc(docTypeDesc);

        String location = serverConfig.getFileLocationPath() + File.separator + "templates" + File.separator + docFile.getOriginalFilename();

        File dest = new File(location);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            docFile.transferTo(dest);
            docType.setDocTemplateLocation(location);
            boolean result = documentService.uploadDocTemplateWithFile(docType);
            docType.setDocTemplateLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, docType);
        } catch (IOException ex) {
            //TODO 记录
            ex.printStackTrace();
            docType.setDocTemplateLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, docType);
        }
    }

    //文档模板更新
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_TEMPLATE_UPDATE})
    @ResponseBody
    @RequestMapping(value = "/document/template_update", method = {RequestMethod.GET, RequestMethod.POST})
    public String docTemplateUpload(Integer docId, MultipartFile docFile) {
        if (docId == null || docFile == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.UPLOAD_FILE_CANNOT_BE_NULL, null);
        }

        DocType docType = documentService.getDocType(docId);

        String location = serverConfig.getFileLocationPath() + File.separator + "templates" + File.separator + docFile.getOriginalFilename();

        File dest = new File(location);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            docFile.transferTo(dest);
            docType.setDocTemplateLocation(location);
            boolean result = documentService.uploadDocTemplate(docType);
            docType.setDocTemplateLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, docType);
        } catch (IOException ex) {
            //TODO 记录
            ex.printStackTrace();
            docType.setDocTemplateLocation("");
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, StaticProperties.RESPONSE_MESSAGE_FAIL, docType);
        }
    }

    //文档模板删除
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_TEMPLATE_UPLOAD})
    @ResponseBody
    @RequestMapping(value = "/document/template_delete", method = {RequestMethod.GET, RequestMethod.POST})
    public String docTemplateDelete(Integer id) {
        if (id == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "请选择需要删除的模板！", null);
        }

        if (!documentService.deleteTemplate(id)) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "系统内部错误！删除失败！请稍后重试！", null);
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
    }

    //文档打包下载（根据项目打包）
    @RequestMapping(value = "/document/backup_project", method = {RequestMethod.GET, RequestMethod.POST})
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_BACKUP})
    public void documentZipByProject(Integer projectId, HttpServletResponse response) throws Exception {

        if (projectId == null) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(ResponseMessage.PROJECT_ID_CANNOT_BE_NULL);
            return;
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 判断是否参与了项目
        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
            // 没有参与项目
            // 判断是否有高级权限
            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
                // 没有高级权限
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().print(ResponseMessage.PERMISSION_ERROR);
                return;
            }
        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        //获取到这个项目的文件位置，本地存储路径由根/项目名/阶段名构成
        String projectName = projectService.getProject(projectId).getProjectName();
        String projectDocPath = serverConfig.getFileLocationPath() + File.separator + projectName;
        File docDir = new File(projectDocPath);
        if (!docDir.exists()) {
            //如果这个路径不存在，表示这个项目没有上传过文档，创建空文件夹
            docDir.mkdirs();
        }

        //开始设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(projectName + ".zip", "UTF-8"));

        //调用工具类
        ZipUtils.toZip(docDir.getPath(), response.getOutputStream(), true);
    }

    //文档打包下载（多个项目打包）
    @RequiresPermissions(value = {PermissionStrings.DOCUMENT_BACKUP})
    @RequestMapping(value = "/document/backup_projects", method = {RequestMethod.GET, RequestMethod.POST})
    public void documentZipByProjects(@RequestParam("projectIds") List<Integer> projectIds, HttpServletResponse response) throws Exception {

        if (projectIds.size() == 0) {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(ResponseMessage.PROJECT_ID_CANNOT_BE_NULL);
            return;
        }

        //创建临时目录文件夹
        String tempPathString = serverConfig.getFileLocationPath() + File.separator + "tempDirectory";
        File tempPath = new File(tempPathString);
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }

        FileOutputStream fileOutputStream;

        //生成每个项目的压缩包，放到临时目录下
        for (int i = 0; i < projectIds.size(); i++) {
            Integer projectId = projectIds.get(i);
            //获取到这个项目的文件位置，本地存储路径由根/项目名/阶段名构成
            String projectName = projectService.getProject(projectId).getProjectName();
            String projectDocPath = serverConfig.getFileLocationPath() + File.separator + projectName;
            File docDir = new File(projectDocPath);
            if (!docDir.exists()) {
                //如果这个路径不存在，表示这个项目没有上传过文档，创建空文件夹
                docDir.mkdirs();
            }

            //创建输出的临时文件
            File tempFile = new File(serverConfig.getFileLocationPath() + File.separator + "tempDirectory" + File.separator + projectName + ".zip");

            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }

            //创建输出流
            fileOutputStream = new FileOutputStream(tempFile);

            //打包到临时文件夹
            ZipUtils.toZip(docDir.getPath(), fileOutputStream, true);
        }

        //开始设置响应头
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("all-projects.zip", "UTF-8"));

        //调用工具类
        ZipUtils.toZip(tempPath.getPath(), response.getOutputStream(), true);

        //清除临时文件夹
        File[] listFilesTemp = tempPath.listFiles();
        for (int i = 0; i < listFilesTemp.length; i++) {
            listFilesTemp[i].delete();
        }
        tempPath.delete();
    }

    // 在线编辑，获取word文档转换的html
    @ResponseBody
    @RequestMapping(value = "/document/online_edit", method = {RequestMethod.GET, RequestMethod.POST})
    public String onlineEdit(Integer id) throws ParserConfigurationException, TransformerException, IOException {
        if (id == null) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, "");
        }

        Document doc = documentService.getDoc(id);
        if (doc.getIsTemplate().intValue() == 0) {
            // 非模板，是提交的文档
            if (doc.getDocState().intValue() != 1) {
                // 非正常状态，不可编辑
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, "");
            }

            // 正常状态
            File file = new File(doc.getDocLocation());
            if (!file.exists()) {
                // 文件不存在
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, "");
            }
            String content = "null";
            if (file.getName().endsWith(".doc")) {
                // 如果是以doc结尾
                content = Word2HtmlUtils.Word2003ToHtml(file, "");
            } else if (file.getName().endsWith(".docx")) {
                // docx类型
                content = Word2HtmlUtils.Word2007ToHtml(file, "");
            } else {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "该文档不建议在线编辑！", "");
            }
            System.out.println(content);
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, content);
        } else {
            // isTemplate = 1，表示是模板，应加载模板
            String docTypeName = doc.getDocType();
            DocType docType = documentService.getDocTypeByTemplateName(docTypeName);

            if (docType == null) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "模板文档不存在！请手动上传！", "");
            }

            File file = new File(docType.getDocTemplateLocation());
            if (!file.exists()) {
                // 文件不存在
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, "");
            }
            String content = "null";
            if (file.getName().endsWith(".doc")) {
                // 如果是以doc结尾
                content = Word2HtmlUtils.Word2003ToHtml(file, "");
            } else if (file.getName().endsWith(".docx")) {
                // docx类型
                content = Word2HtmlUtils.Word2007ToHtml(file, "");
            } else {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "该文档不建议在线编辑！", "");
            }
            System.out.println(content);
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, content);
        }
    }

    // 在线编辑，保存内容
    @ResponseBody
    @RequestMapping(value = "/document/online_save", method = {RequestMethod.GET, RequestMethod.POST})
    public String onlineEditSave(String content, Integer id) throws IOException {

        // 这里判断id
        if (id == null) {
            // 如果id为空，直接返回错误
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, null);
        }

        // 根据id获取到document对象
        Document doc = documentService.getDoc(id);

        // 确认项目处于进行状态
        if (projectService.getProject(doc.getProjectId()).getProjectState().intValue() != 1) {
            return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "项目非处于进行状态，无法提交文档！", null);
        }

        // 获取登录用户
//        Subject subject = SecurityUtils.getSubject();
//        Session session = subject.getSession();
//        User userInLogin = (User) session.getAttribute(StaticProperties.SESSION_USER);

        // 用于测试
        User userInLogin = userService.getUser(84);

        Integer projectId = doc.getProjectId();
        Integer momentId = doc.getMomentId();

        // 判断是否参与了项目
//        if (!projectTeamService.isInProject(projectId, userInLogin.getId())) {
//            // 没有参与项目
//            // 判断是否有高级权限
//            if (!subject.isPermitted(PermissionStrings.PROJECT_READ_ALL)) {
//                // 没有高级权限
//                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.PERMISSION_ERROR, null);
//            }
//        }

        //执行到这里，证明登录的用户参与了项目或者拥有高级权限

        if (doc.getIsTemplate().intValue() == 0) {
            // 非模板id，即编辑已经存在的提交的文档，此时doc中应该包含有location属性，即文档已经存在于服务器本地
            if (doc.getDocState().intValue() != 1) {
                // state为0，状态不正常，不可读
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.DOCUMENT_ID_CANNOT_BE_NULL, null);
            }

            // state == 1，状态正常，此时location一定有值
            File file = new File(doc.getDocLocation());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                // 文件不存在，重新创建文件并保存
                file.createNewFile();
            }

            if (file.getName().endsWith(".docx")) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, "Because of charge, docx online is not be supported! Please consider to upload file!", null);
            }

            boolean result = Word2HtmlUtils.HtmlToWord(content, doc.getDocLocation());
            if (result) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
            } else {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
            }
        } else if (doc.getIsTemplate().intValue() == 1) {
            // 新上传的在线编辑内容
            short state = 1;  //1表示已上传，状态正常，0表示未上传，2表示已删除
            doc.setDocState(state);
            doc.setUploadTime(new Date());
            doc.setUploadFrom(userInLogin.getId());

            String originalFilename = documentService.getDocTypeByTemplateName(doc.getDocType()).getDocTemplateLocation();
            String firstPath = projectService.getProject(projectId).getProjectName();
            String secondPath = projectService.getMoment(momentId).getMomentDesc();

            int count = doc.getCount() + 1;
            String suffix = "";
            if (count < 10) {
                suffix = "-00" + count;
            } else if (count >= 10 && count <100) {
                suffix = "-0" + count;
            } else{
                suffix = "-" + count;
            }
            String wholeName = doc.getDocName() + suffix;

            String location = serverConfig.getFileLocationPath() + File.separator + firstPath + File.separator + secondPath + File.separator + wholeName + originalFilename.substring(originalFilename.lastIndexOf("."));

            File dest = new File(location);

            //判断文件是否存在
            if (!dest.exists()) {
                dest.createNewFile();
            }

            boolean result = Word2HtmlUtils.HtmlToWord(content, location);
            if (result) {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_SUCCESS, StaticProperties.RESPONSE_MESSAGE_SUCCESS, null);
            } else {
                return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
            }
        }

        return new JsonUtil().JsonInfo(StaticProperties.RESPONSE_STATE_FAIL, ResponseMessage.SYSTEM_ERROR, null);
    }

    // 初始化UEditor，注意：此时的UEditor不具备任何上传任何文件保存任何文件的功能，仅限于纯文字的编辑
    @RequestMapping(value = "/document/editor_config", method = {RequestMethod.POST, RequestMethod.GET})
    public void onlineEditorConfig(String action, MultipartFile upfile, HttpServletResponse response) throws IOException {

        if (action.equals("uploadimage")) {
            String timeStr = Long.toString(new Date().getTime());
            String path = File.separator + "onlineEdit" + File.separator + timeStr + File.separator + upfile.getOriginalFilename();
            String location = "c:" + File.separator + "temp" + path;

            File dest = new File(location);

            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            try {
                upfile.transferTo(dest);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                Map<String, String> result = new HashMap<>();
                result.put("state", "SUCCESS");
                result.put("url", "/temp/onlineEdit/" + timeStr  + "/" + upfile.getOriginalFilename());
                result.put("title", upfile.getOriginalFilename());
                result.put("original", upfile.getOriginalFilename());
                PrintWriter writer = response.getWriter();
                writer.write(JSON.toJSONString(result));
                writer.flush();
                writer.close();

            } catch (IOException ex) {
                //TODO 记录
                ex.printStackTrace();
            }
        } else {
            response.sendRedirect("/onlineEdit/jsp/config.json");
        }
    }

    private boolean isMoreThan24Hours(Date oldDate, Date newDate) {
        long nh = 1000 * 60 * 60; // 一小时毫秒数
        long diff = newDate.getTime() - oldDate.getTime(); // 两个日期毫秒差
        long hours = diff / nh;
        if (hours > 24) {
            return true;
        }
        return false;
    }
}
