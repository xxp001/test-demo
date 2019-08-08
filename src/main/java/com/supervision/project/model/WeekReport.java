package com.supervision.project.model;

import lombok.Data;

import java.util.Date;

@Data
public class WeekReport {
    private Integer id;

    private Integer projectId;

    private Integer uploadFrom;

    private Date uploadTime;

    private String summary;

    private String weekDetail;

    private String askQuestion;
}