package com.supervision.document.model;

import lombok.Data;

import java.util.Date;

@Data
public class Document {
    private Integer id;

    private Integer projectId;

    private Integer momentId;

    private String docName;

    private String fileCode;

    private String docType;

    private String docLocation;

    //提交时间
    private Date uploadTime;

    //提交人
    private Integer uploadFrom;

    //状态，0是未上传，1是已上传，2是上传后删除了
    private Short docState;

    private String workCode;

    private String uploaderName;

    // 这个表示该文档对象是否是模板，1表示是，0表示否
    private Integer isTemplate;

    // 这个文档的完整名称，非模板文档才有，即有文件才有该属性，该属性=docName+suffix
    private String wholeName;

    // 后缀，比如-001
    private String suffix;

    // 该文档在这个模板中，排第几，和后缀相应，后缀-001，则count是1
    private Integer count;

    // 该文档模板在项目阶段的顺序，从1开始
    private Integer orderMoment;
}