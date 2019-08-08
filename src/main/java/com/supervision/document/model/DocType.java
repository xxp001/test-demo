package com.supervision.document.model;

import lombok.Data;

@Data
public class DocType {
    private Integer docTypeId;

    private String docTypeName;

    private String docTypeDesc;

    private String docTemplateLocation;
}