package com.supervision.document.service.serviceImpl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.supervision.document.mapper.DocTypeMapper;
import com.supervision.document.mapper.DocumentMapper;
import com.supervision.document.model.DocType;
import com.supervision.document.model.Document;
import com.supervision.document.service.DocumentService;
import com.supervision.project.mapper.MomentMapper;
import com.supervision.project.mapper.ProjectMilestoneMapper;
import com.supervision.project.mapper.ProjectProcessMapper;
import com.supervision.project.model.Moment;
import com.supervision.project.model.ProjectMilestone;
import com.supervision.project.model.ProjectProcess;
import com.supervision.user.mapper.UserMapper;
import com.supervision.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:doc service Impl
 * @Author:TjSanshao
 * @Create:2019-02-17 02:03
 *
 **/
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DocTypeMapper docTypeMapper;

    @Autowired
    private MomentMapper momentMapper;

    @Autowired
    private ProjectProcessMapper projectProcessMapper;

    @Autowired
    private ProjectMilestoneMapper projectMilestoneMapper;

    @Override
    public Object getAllDocsByProject(int page, int pageSize, Integer projectId) {
        PageHelper.startPage(page, pageSize);
        // 这里是查询isTemplate不是1的文档，即不是文档模板
        List<Document> documents = documentMapper.selectAllByProject(projectId);
        return new PageInfo<>(documents);
    }

    @Override
    public List<Document> getAllDocsByMoment(Integer momentId) {
        List<Document> documents = documentMapper.selectAllByMoment(momentId);
        return documents;
    }

    @Override
    public boolean addDocument(Document document) {

        int row = documentMapper.insertSelective(document);

        if (row < 1) {
            return false;
        }

        return true;
    }

    @Override
    public Document getDoc(Integer docId) {
        return documentMapper.selectByPrimaryKey(docId);
    }

    @Transactional
    @Override
    public boolean uploadDoc(Document document) {

        Document documentWithFile = new Document();

        User user = userMapper.selectByPrimaryKey(document.getUploadFrom());

        documentWithFile.setProjectId(document.getProjectId());  // 设置项目
        documentWithFile.setMomentId(document.getMomentId());  // 设置阶段
        documentWithFile.setDocName(document.getDocName());  // 设置文档名称
        documentWithFile.setFileCode(document.getFileCode());  // 设置文档代码
        documentWithFile.setDocType(document.getDocType());  // 设置文档模板类型
        documentWithFile.setDocLocation(document.getDocLocation());  // 设置文件位置
        documentWithFile.setUploadTime(document.getUploadTime());
        documentWithFile.setUploadFrom(document.getUploadFrom());
        documentWithFile.setDocState(document.getDocState());
        documentWithFile.setWorkCode(user.getWorkCode());
        documentWithFile.setUploaderName(user.getUserRealname());
        documentWithFile.setIsTemplate(0);  // 不是模板，是有文件的文档
        documentWithFile.setCount(document.getCount() + 1);

        // 模板文档同步更新
        document.setCount(document.getCount() + 1);

        // 判断是否是里程碑文件
        ProjectMilestone projectMilestone = projectMilestoneMapper.selectByTemplateName(documentWithFile.getDocType());
        if (projectMilestone != null) {
            // 上传的是里程碑文件
            ProjectProcess projectProcess = projectProcessMapper.selectByProject(documentWithFile.getProjectId());
            // 更新进度
            projectProcess.setProjectProcess(projectMilestone.getMilestoneProcess());
            projectProcessMapper.updateByPrimaryKeySelective(projectProcess);
        }

        if (documentWithFile.getCount() < 10) {
            documentWithFile.setSuffix("-00" + documentWithFile.getCount());
        } else if (documentWithFile.getCount() >= 10) {
            documentWithFile.setSuffix("-0" + documentWithFile.getCount());
        } else {
            documentWithFile.setSuffix("-" + documentWithFile.getCount());
        }
        documentWithFile.setWholeName(documentWithFile.getDocName() + documentWithFile.getSuffix());
        documentWithFile.setOrderMoment(document.getOrderMoment());


        // 这里清空文档模板的属性
        document.setIsTemplate(1);
        short state = 0;
        document.setDocState(state);
        document.setUploadTime(null);
        document.setUploadFrom(null);
        document.setDocLocation(null);

        // 更新moment的uploadedDocument，只有当文档模板的count==1时，更新，即第一次上传时，才更新
        if (document.getCount() == 1) {
            Moment moment = momentMapper.selectByPrimaryKey(document.getMomentId());
            moment.setUploadedFileNumber(moment.getUploadedFileNumber() + 1);
            momentMapper.updateByPrimaryKeySelective(moment);
        }

        int row = documentMapper.updateByPrimaryKeySelective(document);
        documentMapper.insertSelective(documentWithFile);

        if (row < 1) {
            return false;
        }

        return true;
    }

    @Override
    public boolean buildDoc(Document document) {

        Moment moment = momentMapper.selectByPrimaryKey(document.getMomentId());
        moment.setUploadedFileNumber(moment.getUploadedFileNumber() + 1);
        moment.setFileNumber(moment.getFileNumber() + 1);
        momentMapper.updateByPrimaryKeySelective(moment);

        int row = documentMapper.insertSelective(document);

        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean createDoc(Document document) {
        Moment moment = momentMapper.selectByPrimaryKey(document.getMomentId());
        moment.setFileNumber(moment.getFileNumber() + 1);
        momentMapper.updateByPrimaryKeySelective(moment);

        if (document.getDocName() == null) {
            DocType docType = docTypeMapper.selectByTemplateName(document.getDocType());
            if (docType != null) {
                // 设置文档长名称
                document.setDocName(docType.getDocTypeDesc());
            }else{
                document.setDocName("没有名字");
            }
        }

        document.setIsTemplate(1);  // 这个是文档模板
        document.setCount(0);  // 没有上传文件

        // 设置文档在项目阶段的顺序
        List<Document> documents = documentMapper.selectAllByMomentIsTemplate(document.getMomentId());
        if (document.getOrderMoment() == null) {
            // 如果没有指定顺序
            document.setOrderMoment(documents.size() + 1);
        }
        for (int i = 0; i < documents.size(); i++) {
            Document tempDoc = documents.get(i);
            if (tempDoc.getOrderMoment() >= document.getOrderMoment()) {
                tempDoc.setOrderMoment(tempDoc.getOrderMoment() + 1);
                documentMapper.updateByPrimaryKeySelective(tempDoc);
            }
        }

        int row = documentMapper.insertSelective(document);

        if (row > 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteDoc(Integer docId) {
        Document document = documentMapper.selectByPrimaryKey(docId);

        String destLocation = document.getDocLocation();

//        document.setDocLocation(null);
//        short state = 2;  //2表示已上传过但已删除，这里暂时不使用这种做法，因此下面还是会将state设置为0
//        state = 0;
//        document.setDocState(state);
//        document.setUploadFrom(null);
//        document.setUploadTime(null);
//        document.setWorkCode(null);
//        document.setUploaderName(null);
//
//        int row = documentMapper.updateByPrimaryKey(document);

        // 不是用逻辑删除，直接使用物理删除，烦了
        int row = documentMapper.deleteByPrimaryKey(docId);

        //删除了文档后应该将moment中的uploadedFileNumber-1
        Moment moment = momentMapper.selectByPrimaryKey(document.getMomentId());
        moment.setUploadedFileNumber(moment.getUploadedFileNumber() - 1);
        if (moment.getUploadedFileNumber() < 0) {
            moment.setUploadedFileNumber(0);
        }
        momentMapper.updateByPrimaryKeySelective(moment);

        if (row > 0) {
            //清空location之后，删除本地磁盘的存储
            if (destLocation == null) {
                return true;
            }
            File file = new File(destLocation);
            if (file.exists()) {
                file.delete();
            }

            return true;
        }

        return false;
    }

    @Override
    public List<DocType> getAllDocType() {
        return docTypeMapper.selectAll();
    }

    @Override
    public PageInfo<DocType> listDocTypesByPage(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<DocType> docTypes = docTypeMapper.selectAll();
        return new PageInfo<>(docTypes);
    }

    @Override
    public boolean uploadDocTemplate(DocType docType) {
        int row = docTypeMapper.updateByPrimaryKeySelective(docType);
        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean uploadDocTemplateWithFile(DocType docType) {
        int row = docTypeMapper.insertSelective(docType);
        if (row > 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteTemplate(Integer id) {
        int row = docTypeMapper.deleteByPrimaryKey(id);

        //TODO 这里不删除文件，懒

        if (row > 0) {
            return true;
        }
        return false;
    }

    @Override
    public DocType getDocType(int docTypeId) {
        return docTypeMapper.selectByPrimaryKey(docTypeId);
    }

    @Override
    public DocType getDocTypeByTemplateName(String templateName) {
        return docTypeMapper.selectByTemplateName(templateName);
    }

    @Override
    public List<Document> listDocsByMomentIsTemplate(Integer momentId) {
        return documentMapper.selectAllByMomentIsTemplate(momentId);
    }

}
