package com.supervision.library.ViewModel;

import com.supervision.library.model.FirstClass;
import com.supervision.library.model.ThirdClass;
import lombok.Data;

import java.util.List;

/*
 * @Project:SupervisionSystem
 * @Description:view model
 * @Author:TjSanshao
 * @Create:2019-05-03 11:25
 *
 **/
@Data
public class ViewSecondClass {
    private Integer id;

    private String className;

    private String classDesc;

    private String keywords;

    private Integer parentClassId;
    private FirstClass parentClass;

    private List<ViewThirdClass> childClasses;
}
