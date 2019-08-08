package com.supervision.project.model;

import com.supervision.document.model.Document;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:custom model
 * @Author:TjSanshao
 * @Create:2019-02-19 21:49
 *
 **/

@Data
@ApiModel(value = "CustomMoment")
public class CustomMoment {

    private Integer id;

    private Integer projectId;

    private Integer momentOrder;

    private String momentDesc;

    private Short state;

    private Integer fileNumber;

    private Integer uploadedFileNumber;

    private Date expectFinishTime;

    private Date finishTime;

    private Date startTime;

    private String momentName;

    private List<Document> documents;

    private String documentStrings;

}
