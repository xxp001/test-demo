package com.supervision.document.ViewModel;

import com.supervision.document.model.Document;
import lombok.Data;

import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-04-11 22:41
 *
 **/
@Data
public class ViewDocumentByType {

    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private String docName;

    private String fileCode;

    private String docType;

    private List<Document> documents;

}
