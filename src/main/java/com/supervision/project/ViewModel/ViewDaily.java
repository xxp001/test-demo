package com.supervision.project.ViewModel;

import com.supervision.project.model.Project;
import com.supervision.user.model.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-02-18 00:59
 *
 **/
@Data
public class ViewDaily {

    private Integer id;

    private Project project;

    private User uploadFrom;

    private Date uploadTime;

    private String summary;

    private String dailyDetail;

    private String dailyDetailAM;

    private String dailyDetailPM;
}
