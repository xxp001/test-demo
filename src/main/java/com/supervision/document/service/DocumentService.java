package com.supervision.document.service;

import com.github.pagehelper.PageInfo;
import com.supervision.document.model.DocType;
import com.supervision.document.model.Document;

import java.util.List;

public interface DocumentService {

    Object getAllDocsByProject(int page, int pageSize, Integer projectId);

    List<Document> getAllDocsByMoment(Integer momentId);

    boolean addDocument(Document document);

    Document getDoc(Integer docId);

    boolean uploadDoc(Document document);

    boolean buildDoc(Document document);

    boolean createDoc(Document document);

    boolean deleteDoc(Integer docId);

    List<DocType> getAllDocType();

    PageInfo<DocType> listDocTypesByPage(Integer page, Integer pageSize);

    boolean uploadDocTemplate(DocType docType);

    boolean uploadDocTemplateWithFile(DocType docType);

    boolean deleteTemplate(Integer id);

    DocType getDocType(int docTypeId);

    DocType getDocTypeByTemplateName(String templateName);

    List<Document> listDocsByMomentIsTemplate(Integer momentId);

}
