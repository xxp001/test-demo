package com.supervision.project.ViewModel;

import com.supervision.project.model.Daily;
import com.supervision.project.model.HumanCostRecord;
import lombok.Data;

import java.util.Date;
import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-03-14 00:46
 *
 **/
@Data
public class ViewDailyAndHumanCost {

    private String dailyList;

    private String costRecordList;

    private List<ViewDaily> viewDailyList;

    private Date uploadTime;

    private List<Daily> dailyListObject;

    private List<HumanCostRecord> costRecordListObject;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;

    private Integer pages;

}
