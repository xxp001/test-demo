package com.supervision.library.ViewModel;

import lombok.Data;

import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-05-03 11:58
 *
 **/
@Data
public class ViewFirstClass {
    private Integer id;

    private String className;

    private String classDesc;

    private String keywords;

    private List<ViewSecondClass> childClasses;
}
