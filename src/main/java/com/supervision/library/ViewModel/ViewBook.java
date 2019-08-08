package com.supervision.library.ViewModel;

import com.supervision.library.model.FirstClass;
import com.supervision.library.model.SecondClass;
import com.supervision.library.model.ThirdClass;
import com.supervision.user.model.User;
import lombok.Data;

import java.util.Date;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-05-03 10:56
 *
 **/
@Data
public class ViewBook {
    private Integer id;

    private String bookName;

    private String bookDesc;

    private String bookKeywords;

    private Integer firstClassId;
    private FirstClass firstClass;

    private Integer secondClassId;
    private SecondClass secondClass;

    private Integer thirdClassId;
    private ThirdClass thirdClass;

    private String bookLocation;

    private Date editTime;

    private User uploadFrom;

    private String uploadName;
}
