package com.supervision.library.model;

import lombok.Data;

import java.util.Date;

@Data
public class Book {
    private Integer id;

    private String bookName;

    private String bookDesc;

    private String bookKeywords;

    private Integer firstClass;

    private Integer secondClass;

    private Integer thirdClass;

    private String bookLocation;

    private Date editTime;

    private Integer uploadFrom;
}