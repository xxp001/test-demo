package com.supervision.project.ViewModel;

import com.supervision.project.model.ExtraCost;
import com.supervision.project.model.HumanCostRecord;
import lombok.Data;

import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-03-24 13:21
 *
 **/
@Data
public class ViewCost {

    private Integer projectId;

    private Integer momentId;

    private Date uploadTime;

    private Integer uploadFrom;

    private HumanCostRecord humanCostRecord;

    //这里前端使用字符串上传数组
    private String extraCostList;

}
