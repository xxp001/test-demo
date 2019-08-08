package com.supervision.project.model;

import lombok.Data;

import java.util.Date;

@Data
public class Daily {
    private Integer id;

    private Integer projectId;

    private Integer uploadFrom;

    private Date uploadTime;

    private String summary;

    private String dailyDetail;

    private String dailyDetailAM;

    private String dailyDetailPM;
}